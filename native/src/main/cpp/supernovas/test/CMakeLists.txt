# Additional CMake configuration file for building the regression testing programs
# of the SuperNOVAS library. The programs are added to the test suite.
#
# To invoke simply configure the cmake build in the supernovas directory with
# the -DBUILD_TESTING=ON option.
#
# Author: Attila Kovacs

include_directories(${supernovas_INCLUDE_DIRS} ${PROJECT_SOURCE_DIR}/legacy)

# Link against binaries in the local supernovas lib/ directory
link_directories(${CMAKE_BINARY_DIR}/lib)

add_subdirectory(c99)

if(ENABLE_CPP)
    add_subdirectory(cpp)
endif()



