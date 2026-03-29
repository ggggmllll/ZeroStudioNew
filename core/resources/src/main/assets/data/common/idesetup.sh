#!/bin/bash

set -eu

Color_Off='\033[0m'
Red='\033[0;31m'
Green='\033[0;32m'
Blue='\033[0;34m'
Orange="\e[38;5;208m"

yes='^[Yy][Ee]?[Ss]?$'

# Defualt values
arch=$(uname -m)
install_dir=$HOME
sdkver_org=35.0.2
ndkver_org=29.0.14033849
cmakever_org=4.1.2
with_cmdline=true
assume_yes=false
manifest="https://github.com/msmt2018/SDK-tool-for-Android-platform/releases/download/IDESdkDownJson2.3/manifest.json"
pkgm="pkg"
pkg_curl="libcurl"
pkgs="jq tar unzip"
jdk_version="17"

print_info() {
  # shellcheck disable=SC2059
  printf "${Blue}$1$Color_Off\n"
}

print_err() {
  # shellcheck disable=SC2059
  printf "${Red}$1$Color_Off\n"
}

print_warn() {
  # shellcheck disable=SC2059
  printf "${Orange}$1$Color_Off\n"
}

print_success() {
  # shellcheck disable=SC2059
  printf "${Green}$1$Color_Off\n"
}

is_yes() {

  msg=$1

  printf "%s ([y]es/[n]o): " "$msg"

  if [ "$assume_yes" == "true" ]; then
    ans="y"
    echo $ans
  else
    read -r ans
  fi

  if [[ "$ans" =~ $yes ]]; then
    return 0
  fi

  return 1
}

check_arg_value() {
  option_name="$1"
  arg_value="$2"
  if [[ -z "$arg_value" ]]; then
    print_err "No value provided for $option_name!" >&2
    exit 1
  fi
}

check_command_exists() {
  if command -v "$1" &>/dev/null; then
    return
  else
    print_err "Command '$1' not found!"
    exit 1
  fi
}

# shellcheck disable=SC2068
install_packages() {
  if [ "$assume_yes" == "true" ]; then
    $pkgm install $@ -y
  else
    $pkgm install $@
  fi
}

install_p7zip() {
  print_info "Installing p7zip manually for 7z extraction support..."
  p7zip_url=""
  case "$arch" in
    "aarch64") p7zip_url="https://github.com/msmt2018/termux-packages/releases/download/p7zip-17.06-1/debs-aarch64-e9f3af7af65c6f737f41404dbd6babf727147861.deb" ;;
    "arm") p7zip_url="https://github.com/msmt2018/termux-packages/releases/download/p7zip-17.06-1/debs-arm-e9f3af7af65c6f737f41404dbd6babf727147861.deb" ;;
    "x86_64") p7zip_url="https://github.com/msmt2018/termux-packages/releases/download/p7zip-17.06-1/debs-x86_64-e9f3af7af65c6f737f41404dbd6babf727147861.deb" ;;
  esac

  if [ -n "$p7zip_url" ]; then
    tmp_p7zip_dir="$install_dir/tmp_p7zip_$$"
    mkdir -p "$tmp_p7zip_dir"
    cd "$tmp_p7zip_dir"
    curl -L -o "p7zip.zip" "$p7zip_url" --http1.1
    unzip -q p7zip.zip
    
    apt install ./debs/*.deb -y || dpkg -i ./debs/*.deb
    
    cd - > /dev/null
    rm -rf "$tmp_p7zip_dir"
    print_success "p7zip installed successfully."
  else
    print_warn "No p7zip available for $arch"
  fi
  echo ""
}

print_help() {
  echo "AndroidIDE build tools installer"
  echo "This script helps you easily install build tools in AndroidIDE."
  echo ""
  echo "Usage:"
  echo "${0} -s 35.0.2 -c -j 17"
  echo "This will install Android SDK 35.0.2 with command line tools and JDK 17."
  echo ""
  echo "Options :"
  echo "-i   Set the installation directory. Defaults to \$HOME."
  echo "-s   Android SDK version to download."
  echo "-n   Android NDK version to download."
  echo "-k   Android CMake version to download."
  echo "-c   Download Android SDK with command line tools."
  echo "-m   Manifest file URL. Defaults to 'manifest.json' in 'androidide-tools' GitHub repository."
  echo "-j   OpenJDK version to install. Values can be '17' or '21'"
  echo "-g   Install package: 'git'."
  echo "-o   Install package: 'git'."
  echo "-y   Assume \"yes\" as answer to all prompts and run non-interactively."
  echo ""
  echo "For testing purposes:"
  echo "-a   CPU architecture. Extracted using 'uname -m' by default."
  echo "-p   Package manager. Defaults to 'pkg'."
  echo "-l   Name of curl package that will be installed before starting installation process. Defaults to 'libcurl'."
  echo ""
  echo "-h   Prints this message."
}

download_and_extract() {
  # Display name to use in print messages
  name=$1

  # URL to download from
  url=$2

  # Directory in which the downloaded archive will be extracted
  dir=$3

  # Destination path for downloading the file
  dest=$4

  if [ ! -d "$dir" ]; then
    mkdir -p "$dir"
  fi

  cd "$dir"

  do_download=true
  if [ -f "$dest" ]; then
    name=$(basename "$dest")
    print_info "File ${name} already exists."
    if is_yes "Do you want to skip the download process?"; then
      do_download=false
    fi
    echo ""
  fi

  if [ "$do_download" = "true" ]; then
    print_info "Downloading $name from $url..."
    curl -L -o "$dest" "$url" --http1.1
    print_success "$name has been downloaded."
    echo ""
  fi

  if [ ! -f "$dest" ]; then
    print_err "The downloaded file $name does not exist. Cannot proceed..."
    exit 1
  fi

  print_info "Extracting downloaded archive..."
  
  # 创建临时目录用于解压
  tmp_dir="${dir}/tmp_extract_$$"
  mkdir -p "$tmp_dir"

  # 根据扩展名动态解压
  if [[ "$dest" == *.zip ]]; then
    unzip -q "$dest" -d "$tmp_dir"
  elif [[ "$dest" == *.7z ]]; then
    7z x "$dest" -o"$tmp_dir"
  elif [[ "$dest" == *.tar.gz ]] || [[ "$dest" == *.tgz ]]; then
    tar xvzf "$dest" -C "$tmp_dir"
  elif [[ "$dest" == *.tar.xz ]]; then
    tar xvJf "$dest" -C "$tmp_dir"
  else
    tar xvf "$dest" -C "$tmp_dir"
  fi

  # 剥离多余的顶层文件夹（例如将 android-ndk-r29c 下的内容直接上提）
  inner_count=$(ls -1 "$tmp_dir" | wc -l)
  if [ "$inner_count" -eq 1 ]; then
    inner_dir="$tmp_dir/$(ls -1 "$tmp_dir")"
    if [ -d "$inner_dir" ]; then
      cp -r "$inner_dir"/* "$dir"/ 2>/dev/null || true
    else
      cp -r "$tmp_dir"/* "$dir"/ 2>/dev/null || true
    fi
  else
    cp -r "$tmp_dir"/* "$dir"/ 2>/dev/null || true
  fi
  
  rm -rf "$tmp_dir"
  print_info "Extracted successfully"
  echo ""

  rm -vf "$dest"
  cd - > /dev/null
}

download_comp() {
  nm=$1
  jq_query=$2
  mdir=$3
  # dname=$4

  # Extract the Android SDK URL
  print_info "Extracting URL for $nm from manifest..."
  url=$(jq -r "${jq_query}" "$downloaded_manifest")
  
  # 拦截无效链接
  if [ "$url" == "x" ] || [ "$url" == "null" ] || [ -z "$url" ]; then
    print_warn "Component $nm is not available for architecture $arch. Skipping..."
    echo ""
    return
  fi

  print_success "Found URL: $url"
  echo ""

  # 动态根据 url 生成目标文件名，确保应对 .zip, .7z, .tar.xz
  filename=$(basename "$url")
  filename="${filename%%\?*}"
  dest="$mdir/$filename"

  download_and_extract "$nm" "$url" "$mdir" "$dest"
}

## NOTE!
## When adding more installation configuration arguments,
# add them in com.itsaky.andridide.models.IdeSetupArgument as well
while [ $# -gt 0 ]; do
  case $1 in
  -c | --with-cmdline-tools)
    shift
    with_cmdline=false
    ;;
  -g | --with-git)
    shift
    pkgs+=" git"
    ;;
  -o | --with-openssh)
    shift
    pkgs+=" openssh"
    ;;
  -y | --assume-yes)
    shift
    assume_yes=true
    ;;
  -i | --install-dir)
    shift
    check_arg_value "--install-dir" "${1:-}"
    install_dir="$1"
    ;;
  -m | --manifest)
    shift
    check_arg_value "--manifest" "${1:-}"
    manifest="$1"
    ;;
  -s | --sdk)
    shift
    check_arg_value "--sdk" "${1:-}"
    sdkver_org="$1"
    ;;
  -n | --ndk)
    shift
    check_arg_value "--ndk" "${1:-}"
    ndkver_org="$1"
    ;;
  -k | --cmake)
    shift
    check_arg_value "--cmake" "${1:-}"
    cmakever_org="$1"
    ;;
  -j | --jdk)
    shift
    check_arg_value "--jdk" "${1:-}"
    jdk_version="$1"
    ;;
  -a | --arch)
    shift
    check_arg_value "--arch" "${1:-}"
    arch="$1"
    ;;
  -p | --package-manager)
    shift
    check_arg_value "--package-manager" "${1:-}"
    pkgm="$1"
    ;;
  -l | --curl)
    shift
    check_arg_value "--curl" "${1:-}"
    pkg_curl="$1"
    ;;
  -h | --help)
    print_help
    exit 0
    ;;
  -*)
    echo "Invalid option: $1" >&2
    exit 1
    ;;
  *) break ;;
  esac
  shift
done

if [ "$arch" = "armv7l" ]; then
  arch="arm"
fi

# 64-bit CPU in 32-bit mode
if [ "$arch" = "armv8l" ]; then
  arch="arm"
fi

check_command_exists "$pkgm"

if [ "$jdk_version" == "21" ]; then
  print_warn "OpenJDK 21 support in AndroidIDE is experimental. It may or may not work properly."
  print_warn "Also, OpenJDK 21 is only supported in Gradle v8.4 and newer. Older versions of Gradle will NOT work!"
  if ! is_yes "Do you still want to install OpenJDK 21?"; then
    jdk_version="17"
    print_info "OpenJDK version has been reset to '17'"
  fi
fi

if [ "$jdk_version" != "17" ] && [ "$jdk_version" != "21" ]; then
  print_err "Invalid JDK version '$jdk_version'. Value can be '17' or '21'."
  exit 1
fi

sdk_version="_${sdkver_org//'.'/'_'}"
ndk_version="_${ndkver_org//'.'/'_'}"
cmake_version="_${cmakever_org//'.'/'_'}"

pkgs+=" $pkg_curl"

echo "------------------------------------------"
echo "Installation directory    : ${install_dir}"
echo "SDK version               : ${sdkver_org}"
echo "NDK version               : ${ndkver_org}"
echo "CMake version             : ${cmakever_org}"
echo "JDK version               : ${jdk_version}"
echo "With command line tools   : ${with_cmdline}"
echo "Extra packages            : ${pkgs}"
echo "CPU architecture          : ${arch}"
echo "------------------------------------------"

if ! is_yes "Confirm configuration"; then
  print_err "Aborting..."
  exit 1
fi

if [ ! -f "$install_dir" ]; then
  print_info "Installation directory does not exist. Creating directory..."
  mkdir -p "$install_dir"
fi

if [ ! command -v "$pkgm" ] &>/dev/null; then
  print_err "'$pkgm' command not found. Try installing 'termux-tools' and 'apt'."
  exit 1
fi

# Update repositories and packages
print_info "Update packages..."

$pkgm update
if [ "$assume_yes" == "true" ]; then
  $pkgm upgrade -y
else
  $pkgm upgrade
fi

# Install required packages
print_info "Installing required packages.."
# shellcheck disable=SC2086
install_packages $pkgs && print_success "Packages installed"
echo ""

install_p7zip

# Download the manifest.json file
print_info "Downloading manifest file..."
downloaded_manifest="$install_dir/manifest.json"
curl -L -o "$downloaded_manifest" "$manifest" && print_success "Manifest file downloaded"
echo ""

# Install the Android SDK
download_comp "Android SDK" ".android_sdk" "$install_dir/android-sdk"

# Install build tools
download_comp "Android SDK Build Tools" ".build_tools | .${arch} | .${sdk_version}" "$install_dir/android-sdk/build-tools"

# Install platform tools
download_comp "Android SDK Platform Tools" ".platform_tools | .${arch} | .${sdk_version}" "$install_dir/android-sdk" "android-sdk-platform-tools"

# Install NDK (解压到 ndk/版本号 目录下)
download_comp "Android NDK" ".android_ndk.\"${arch}\".\"${ndk_version}\"" "$install_dir/android-sdk/ndk/$ndkver_org" "android-ndk"

# Install CMake (解压到 cmake/版本号 目录下)
download_comp "CMake" ".android_cmake | .${arch} | .${cmake_version}" "$install_dir/android-sdk/cmake/$cmakever_org" "android-cmake"

if [ "$with_cmdline" = true ]; then
  download_comp "Command-line tools" ".cmdline_tools" "$install_dir/android-sdk/cmdline-tools"
fi

# Install JDK
print_info "Installing package: 'openjdk-$jdk_version'"
install_packages "openjdk-$jdk_version" && print_info "JDK $jdk_version has been installed."

jdk_dir="$SYSROOT/opt/openjdk"

print_info "Updating ide-environment.properties..."
print_info "JAVA_HOME=$jdk_dir"
echo ""
props_dir="$SYSROOT/etc"
props="$props_dir/ide-environment.properties"

if [ ! -d "$props_dir" ]; then
  mkdir -p "$props_dir"
fi

if [ ! -e "$props" ]; then
  printf "JAVA_HOME=%s" "$jdk_dir" >"$props" && print_success "Properties file updated successfully!"
else
  if is_yes "$props file already exists. Would you like to overwrite it?"; then
    printf "JAVA_HOME=%s" "$jdk_dir" >"$props" && print_success "Properties file updated successfully!"
  else
    print_err "Manually edit $SYSROOT/etc/ide-environment.properties file and set JAVA_HOME and ANDROID_SDK_ROOT."
  fi
fi

rm -vf "$downloaded_manifest"
print_success "================================================="
print_success " Downloads completed. Environment is ready!"
print_success " Returning to IDE automatically..."
print_success "================================================="

exit 0