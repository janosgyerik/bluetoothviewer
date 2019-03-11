#!/bin/bash -e
#
# SCRIPT: build.sh
# AUTHOR: Janos Gyerik <info@titan2x.com>
# DATE:   2013-11-11
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
    echo "  -d, --debug          select debug build, default = $debug"
    echo "  -r, --release        select release build, default = $release"
    echo "      --lite           select the lite version, default = $lite"
    echo "      --full           select the full version, default = $full"
    echo
    echo "  -b, --build          build, default = $build"
    echo "      --clean          clean, default = $clean"
    echo "  -i, --install        install selected app, default = $install"
    echo "  -u, --uninstall      uninstall selected app, default = $uninstall"
    echo "  -s, --start          start selected app, default = $start"
    echo
    echo "  -l, --list           list built apks, default = $list"
    echo
    echo "  -h, --help           Print this help"
    echo
    exit 1
}

args=
#arg=
#flag=off
#param=
debug=off
release=off
lite=off
full=off
build=off
clean=off
install=off
uninstall=off
start=off
list=off
action=off
while [ $# != 0 ]; do
    case $1 in
    -h|--help) usage ;;
    -d|--debug) debug=on ;;
    -r|--release) release=on ;;
    --lite) lite=on; full=off ;;
    --full) full=on; lite=off ;;
    --clean) clean=on ;;
    -b|--build) build=on; action=on ;;
    -i|--install) install=on; uninstall=off; action=on ;;
    -u|--uninstall) uninstall=on; install=off; start=off; action=on ;;
    -s|--start) start=on; action=on ;;
    -l|--list) list=on; action=on ;;
    --) shift; while [ $# != 0 ]; do args="$args \"$1\""; shift; done; break ;;
    -) usage "Unknown option: $1" ;;
    -?*) usage "Unknown option: $1" ;;
    *) args="$args \"$1\"" ;;  # script that takes multiple arguments
#    *) test "$arg" && usage || arg=$1 ;;  # strict with excess arguments
#    *) arg=$1 ;;  # forgiving with excess arguments
    esac
    shift
done

eval "set -- $args"  # save arguments in $@. Use "$@" in for loops, not $@

test $action = on || usage

msg() {
    echo '*' $*
}

randstring() {
    md5sum=$(which md5sum 2>/dev/null || which md5)
    POS=2
    LEN=12
    str=$(echo $1 $$ $(date +%S) | $md5sum | $md5sum)
    echo ${str:$POS:$LEN}
}

list() {
    ls -ltr */build/outputs/apk/{release,debug}/*-{release,debug*}.apk 2>/dev/null
}

run() {
    echo $*
    $*
}

dirname=$(dirname "$0")
config=$dirname/config.sh
test -f "$config" && . "$config"
cd "$dirname"/..

gradle=./gradlew

projectname=$(grep ^include settings.gradle | head -n 1 | sed -e 's/.*://' -e 's/.$//')

keys_config=./keys/config.sh
if test $release = on; then
    test -f $keys_config || {
        mkdir -p keys
        cat<<EOF >$keys_config
#!/bin/sh

export STORE_FILE=$PWD/keys/$projectname.keystore
export STORE_PASSWORD=$(randstring store)
export KEY_ALIAS=mykey
export KEY_PASSWORD=$(randstring key)

# eof
EOF
    }
    test -f keys/$projectname.keystore || {
        . $keys_config
        keytool -genkey -v -keystore keys/$projectname.keystore -storepass $storepass -keypass $keypass -validity 10000 -keyalg RSA
    }
    . $keys_config
fi

if test $build = on; then
    test $clean = on && tasks=clean || tasks=
    test $debug = on && tasks="$tasks assembleDebug"
    test $release = on && tasks="$tasks assembleRelease"
    run $gradle $tasks $*
    echo
    list=on
fi

proj=
if test $lite = on; then
    proj=$projectname-lite
elif test $full = on; then
    proj=$projectname-full
else
    proj=$projectname
fi

apk=
if test "$proj"; then 
    if test $debug = on; then
        apk=$proj/build/outputs/apk/debug/$proj-debug.apk
    elif test $release = on; then
        apk=$proj/build/outputs/apk/release/$proj-release.apk
    fi
fi

if test $install = on; then
    run adb -d install -r $apk
elif test $uninstall = on; then
    if test $lite = on; then
        run adb uninstall com.$projectname.lite
    elif test $full = on; then
        run adb uninstall com.$projectname.full
    else
        run adb uninstall net.bluetoothviewer
    fi
fi

if test $start = on; then
    #run adb shell am start -n com.$projectname.lite/$activity
    run adb shell am start -n net.bluetoothviewer/$activity
fi

test $list = on && list
