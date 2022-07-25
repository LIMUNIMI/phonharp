cmake_minimum_required(VERSION 3.4.1)

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

#include shared libraries
include_directories(./shared ./)

# Build our own native library
#add_library (native-lib SHARED native-lib.cpp SoundBoardEngine.cpp)

# Specify the libraries which our native library is dependent on, including Oboe
#target_link_libraries(native-lib log oboe::oboe)

add_library( OboeSynthMain SHARED
        native-lib.cpp
        OboeSynthMain.cpp
        SmoothedParameter.h
        SmoothedAmpParameter.h
        SmoothedFrequency.h
        Oscillators.h
        Filters.h
        )

target_link_libraries(
        OboeSynthMain
        log oboe::oboe android)

# Enable optimization flags: if having problems with source level debugging,
# disable -Ofast ( and debug ), re-enable it after done debugging.
target_compile_options(
        OboeSynthMain
        PRIVATE -Wall -Werror -Ofast)