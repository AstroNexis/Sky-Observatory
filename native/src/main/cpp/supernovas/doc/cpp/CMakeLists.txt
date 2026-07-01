# Additional CMake configuration file for building the documentation of the 
# SuperNOVAS library. The examples are also added to the test suite so they 
# may be checked for functionality.
#
# To invoke simply configure the cmake build in the supernovas directory with
# the -DBUILD_DOC=ON -DENABLE_CPP=ON options.
#
# Author: Attila Kovacs

# ----------------------------------------------------------------------------
# Generate derivative documentation

if(Doxygen_FOUND)
    # Generate headless docs and run Doxygen on them.
    doxygen_add_docs(cpp_docs ALL CONFIG_FILE ${CMAKE_CURRENT_SOURCE_DIR}/Doxyfile)
    add_dependencies(cpp_docs headless_readme)
    
     # For html/ dir, list just the directory, not the individual files installed.
    install(CODE "message(STATUS \"Installing: <prefix>/${CMAKE_INSTALL_DOCDIR}/html/cpp/*\")")
    install(DIRECTORY html/
        DESTINATION ${CMAKE_INSTALL_DOCDIR}/html/cpp
        MESSAGE_NEVER
        COMPONENT Development
    )

    # Install Doxygen tag, which downstream docs can link to.
    install(FILES supernovas++.tag
        DESTINATION ${CMAKE_INSTALL_DOCDIR}
        COMPONENT Development
    )
else()
    message(WARNING "Doxygen not found -- Will skip generating C++ HTML documentation")
endif()
    
    

   
