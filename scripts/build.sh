#!/bin/sh -e
#
# SCRIPT: build.sh
# AUTHOR: Janos Gyerik <info@janosgyerik.com>
# DATE:   2011-02-10
# REV:    1.0.D (Valid are A, B, D, T and P)
#               (For Alpha, Beta, Dev, Test and Production)
#
# PLATFORM: Not platform dependent
#
# PURPOSE: Build the project in debug or release mode and install on a device
#
# set -n   # Uncomment to check your syntax, without execution.
#          # NOTE: Do not forget to put the comment back in or
#          #       the shell script will not execute!
# set -x   # Uncomment to debug this shell script (Korn shell only)
#

usage() {
    test $# = 0 || echo $@
    echo "Usage: $0 [OPTION]... [ARG]..."
    echo
    echo Build the project in debug or release mode and install on a device
    echo
    echo Options:
    echo "  -b, --build          to build or not, default = $build"
    echo "  -i, --install        to install or not, default = $install"
    echo "      --uninstall      to uninstall or not, default = $uninstall"
    echo
    echo "  -d, --debug          build for debug, default = $debug"
    echo "  -r, --release        build for release, default = $release"
    echo
    echo "  -u, --usb            install on USB device, default = $usb"
    echo "  -e, --emulator       install on emulator, default = $emulator"
    echo
    echo "      --setup-keys     Setup key store, default = $setup_keys"
    echo
    echo "  -h, --help           Print this help"
    echo
    exit 1
}

args=
#arg=
#flag=off
#param=
build=off
install=off
uninstall=off
debug=off
release=off
usb=off
emulator=off
setup_keys=off
while [ $# != 0 ]; do
    case $1 in
    -h|--help) usage ;;
    -b|--build) build=on ;;
    -i|--install) install=on ;;
    --uninstall) uninstall=on ;;
    -d|--debug) debug=on ;;
    -r|--release) release=on ;;
    -u|--usb) usb=on ;;
    -e|--emulator) emulator=on ;;
    --setup-keys) setup_keys=on ;;
#    --) shift; while [ $# != 0 ]; do args="$args \"$1\""; shift; done; break ;;
    -) usage "Unknown option: $1" ;;
    -?*) usage "Unknown option: $1" ;;
    *) args="$args \"$1\"" ;;  # script that takes multiple arguments
#    *) test "$arg" && usage || arg=$1 ;;  # strict with excess arguments
#    *) arg=$1 ;;  # forgiving with excess arguments
    esac
    shift
done

eval "set -- $args"  # save arguments in $@. Use "$@" in for loops, not $@ 

#test $# = 0 && usage


msg() {
    echo '* '$* ...
}

warn() {
    echo 'WARNING: '$*
}

fatal() {
    echo 'ERROR: '$*
    exit 1
}

randstring() {
    md5sum=$(which md5sum 2>/dev/null || which md5)
    POS=2
    LEN=8
    str=$(echo $1 $$ $(date +%S) | $md5sum | $md5sum)
    echo ${str:$POS:$LEN}
}

cd $(dirname "$0")/..

test -f local.properties -a -f build.xml || {
    msg local.properties or build.xml missing, runnig android update command
    android update project --path .
}

projectname=$(grep project.name build.xml | head -n 1 | sed -e 's/.*project name="\([^"]*\)".*/\1/')
keys_dir=./keys
keys_config=$keys_dir/config.sh

if test $setup_keys = on; then
    test -f $keys_config || {
        mkdir -p $keys_dir
        cat<<EOF >$keys_config
#!/bin/sh

keystore=$keys_dir/$projectname.keystore
alias=mykey

storepass=$(randstring store)
keypass=$(randstring key)

# eof
EOF
    }
    . $keys_config
    test -f $keystore || {
        keytool -genkey -v -keystore $keystore -storepass $storepass -keypass $keypass -validity 10000 -keyalg RSA
    }
fi

if test $build = on; then
    if test $debug = on; then
        ant debug
    fi
    if test $release = on; then
        if ! test -f $keys_config; then
            warn 'Key store configuration does not exist.'
            warn 'This probably means you are trying to create a release'
            warn 'build for the very first time. Re-run this script with'
            warn 'the --setup-keys flag to create a key store, and then'
            warn 'try to create the release build again.'
            fatal 'Cannot create release build without key store, exiting.'
        fi
        msg ant build
        ant release
        msg jarsigner
        . $keys_config
        jarsigner -verbose -keystore $keystore -storepass $storepass -keypass $keypass bin/$projectname-release-unsigned.apk $alias
        msg zipalign
        rm -f bin/$projectname-release.apk
        zipalign -v 4 bin/$projectname-release-unsigned.apk bin/$projectname-release.apk
    fi
fi

if test $install = on; then
    if test $debug = on; then
        target=bin/$projectname-debug.apk
        test $usb = on && adb -d install -r $target
        test $emulator = on && adb -e install -r $target
    fi
    if test $release = on; then
        target=bin/$projectname-release.apk
        test $usb = on && adb -d install -r $target
        test $emulator = on && adb -e install -r $target
    fi
fi

if test $uninstall = on; then
    package=$(grep package= AndroidManifest.xml | sed -e s/.*package=// | cut -f2 -d\")
    adb uninstall $package
fi

# eof
