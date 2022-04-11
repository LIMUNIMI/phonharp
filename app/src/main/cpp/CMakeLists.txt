cmake_minimum_required(VERSION 3.4.1)

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

#include shared libraries
include_directories(./shared ./)

# Build our own native library
#add_library (native-lib SHARED native-lib.cpp SoundBoardEngine.cpp)

# Specify the libraries which our native library is dependent on, including Oboe
#target_link_libraries(native-lib log oboe::oboe)

add_library( soundboard SHARED
        native-lib.cpp
        SoundBoardEngine.cpp
        )

target_link_libraries( soundboard log oboe::oboe )