# Additional CMake configuration file for building the documentation of the 
# SuperNOVAS library.
#
# To invoke simply configure the cmake build in the supernovas directory with
# the -DBUILD_DOC=ON option.
#
# Author: Attila Kovacs

# ----------------------------------------------------------------------------
# Generate derivative documentation

# Generate headless README
add_executable(docedit EXCLUDE_FROM_ALL src/docedit.c)

add_custom_target(headless_readme ALL
    COMMAND docedit ${CMAKE_CURRENT_SOURCE_DIR}
)

# Install Markdown documentation (Development)
file(GLOB MD_FILES doc/*.md)
install(FILES ${MD_FILES}
    COMPONENT Development
    DESTINATION ${CMAKE_INSTALL_DOCDIR}
)

find_package(Doxygen)

set_package_properties(Doxygen PROPERTIES
    URL "https://doxygen.nl/"
    DESCRIPTION  "is a widely-used documentation generator tool in software development"
    TYPE OPTIONAL
    PURPOSE "Enables HTML documentation for supernovas"
)

add_subdirectory(c99)
    
if(ENABLE_CPP)
    add_subdirectory(cpp)
endif()
    

   
