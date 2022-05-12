cmake_minimum_required(VERSION 3.4.1)

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

#include shared libraries
include_directories(./shared ./)

# Build our own native library
#add_library (native-lib SHARED native-lib.cpp SoundBoardEngine.cpp)

# Specify the libraries which our native library is dependent on, including Oboe
#target_link_libraries(native-lib log oboe::oboe)

add_library( soundboard SHARED OboeSinePlayer
        native-lib.cpp
        SoundBoardEngine.cpp
        OboeSinePlayer.cpp
        SmoothedParameter.h
        SmoothedAmpParameter.h
        SmoothedFrequency.h
        )

target_link_libraries(soundboard log oboe::oboe android)

# Enable optimization flags: if having problems with source level debugging,
# disable -Ofast ( and debug ), re-enable it after done debugging.
target_compile_options(soundboard PRIVATE -Wall -Werror -Ofast)