cmake_minimum_required(VERSION 3.22.1)

project("noisysynth")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find the Oboe package FIRST (AAR provides this via prefab)
find_package(oboe REQUIRED CONFIG)

# Create our library
add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    SynthEngine.cpp
    SynthEngine.h
)

# Link libraries - use oboe::oboe (with namespace)
target_link_libraries(${CMAKE_PROJECT_NAME}
    android
    log
    oboe::oboe
)
