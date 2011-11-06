#!/bin/sh
#
# SCRIPT: build.sh
# AUTHOR: Janos Gyerik <info@titan2x.com>
# DATE:   2011-02-10
# REV:    1.0.D (Valid are A, B, D, T and P)
#               (For Alpha, Beta, Dev, Test and Production)
#
# PLATFORM: Not platform dependent
#
# PURPOSE: Build the project in debug or release mode
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
    echo Build the project in debug or release mode
    echo
    echo Options:
    echo "  -b, --build          to build or not, default = $build"
    echo "  -i, --install        to install or not, default = $install"
    echo
    echo "  -d, --debug          build for debug, default = $debug"
    echo "  -r, --release        build for release, default = $release"
    echo
    echo "  -u, --usb            install on USB device, default = $usb"
    echo "  -e, --emulator       install on emulator, default = $emulator"
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
debug=off
release=off
usb=off
emulator=off
while [ $# != 0 ]; do
    case $1 in
    -h|--help) usage ;;
    -b|--build) build=on ;;
    -i|--install) install=on ;;
    -d|--debug) debug=on ;;
    -r|--release) release=on ;;
    -u|--usb) usb=on ;;
    -e|--emulator) emulator=on ;;
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

test $build = on -o $install = on && test $debug = on -o $release = on || usage


msg() {
    echo '* '$* ...
}

cd $(dirname "$0")/..

projectname=$(basename "$PWD")

if ! test -f local.properties -a -f build.xml; then
    android update project --name $projectname --target $(. default.properties; echo $target) --path .
fi

if test $build = on; then
    if test $debug = on; then
	ant debug
    fi
    if test $release = on; then
	if ! test -d keys; then
	    echo You must create ./keys first.
	    echo Copy from ./samples/keys to ./keys and customize ./keys/config.sh
	    echo Run: keytool -genkey -v -keystore keys/$projectname.keystore -validity 10000 -keyalg RSA
	    echo Exit.
	    exit 1
	fi
	msg ant build
	ant release
	msg jarsigner
	. keys/config.sh
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

# eof
