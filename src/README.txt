# Source Code Structure

This directory contains placeholder source files for the Ramscoop mod. To complete the source code:

1. Extract the actual source code from the JAR file using a decompiler tool like:
   - JD-GUI (http://java-decompiler.github.io/)
   - CFR (https://www.benf.org/other/cfr/)
   - Procyon (https://bitbucket.org/mstrobel/procyon)

2. Replace the placeholder files in this directory with the extracted source code

3. Compile the code using the build script (build.bat) or Ant (build.xml)

The main classes of the mod are:
- ModPlugin.java: Entry point for the mod, handles initialization
- Ramscoop.java: Main implementation of the resource generation system

For Starsector mod development resources, visit:
- Official Modding Forum: https://fractalsoftworks.com/forum/index.php?board=1.0
- Modding Documentation: https://starsector.fandom.com/wiki/Modding
