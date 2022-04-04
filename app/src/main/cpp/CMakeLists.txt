cmake_minimum_required(VERSION 3.4.1)

#include shared libraries
include_directories(./shared)

# Build our own native library
add_library (native-lib SHARED native-lib.cpp MegaDroneEngine.cpp)

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

# Specify the libraries which our native library is dependent on, including Oboe
target_link_libraries(native-lib log oboe::oboe)
