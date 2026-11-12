MANUAL IMPLEMENTATION FILES
===========================

This directory contains staged .txt files only. No original project source, HTML, batch, data, configuration, or workflow file is changed by these staging files. For each file below, copy the complete text contents into the listed destination (omit the staging .txt suffix when creating the destination file). Keep the existing originals until you have reviewed and tested the replacements.

COMPLETE FILE CONTENTS
----------------------
1. HtmlServer.java.txt
   Destination: HotelRoomService/src/hotel/app/HtmlServer.java
   New local offline HTTP backend. Uses the existing ShopService and repositories; staff writes require a server-checked login session.

2. AppContext.java.txt
   Destination: HotelRoomService/src/hotel/app/AppContext.java
   Complete replacement preserving the existing composition root and adding a configurable base folder and same-JVM shared storage lock.

3. StorageLock.java.txt
   Destination: HotelRoomService/src/hotel/data/StorageLock.java
   Complete replacement preserving the existing cross-process file lock and reentrant behavior.

4. CustomerApp.java.txt
   Destination: HotelRoomService/src/hotel/app/CustomerApp.java
   Complete replacement based on the existing Customer Swing app. Default launch starts/reuses the server and opens customer.html; pass --swing to launch the retained Swing interface.

5. StaffApp.java.txt
   Destination: HotelRoomService/src/hotel/app/StaffApp.java
   Complete replacement based on the existing Staff Swing app. Default launch starts/reuses the server and opens staff.html; pass --swing to launch the retained Swing interface.

6. customer.html.txt
   Destination: HotelRoomService/www/customer.html
   Customer menu, search/category filters, cart, ordering and order tracking.

7. staff.html.txt
   Destination: HotelRoomService/www/staff.html
   Staff login, order/status management, menu stock/price edits, item creation/removal, and image uploads.

8. WebFlowSmokeTest.java.txt
   Destination: HotelRoomService/src/hotel/tests/WebFlowSmokeTest.java
   Real HTTP smoke test against an isolated temporary data directory; it copies the actual HTML files into the temporary server root and cleans up the server afterwards.

9. windows-build.yml.txt
   Destination: .github/workflows/windows-build.yml
   Windows runner compiles, runs behavior checks, makes Customer/Staff self-contained jpackage app-images, zips them and uploads an artifact.

10. Customer.bat.txt
    Destination: HotelRoomService/Customer.bat
    Launcher for the new default browser interface; use CustomerSwing.bat (included in the text) to start the preserved Swing UI.

11. Staff.bat.txt
    Destination: HotelRoomService/Staff.bat
    Launcher for the new default browser interface; use StaffSwing.bat (included in the text) to start the preserved Swing UI.

12. README.txt.txt
    Destination: HotelRoomService/README.txt
    Complete updated usage, data-preservation, offline, test, and packaging instructions.

APPLICATION ORDER
-----------------
After review, apply in this order: StorageLock.java, AppContext.java, HtmlServer.java, CustomerApp.java and StaffApp.java, HTML files, batch files, README, test, and workflow. Make HotelRoomService/www if it does not exist. Keep the existing `data/`, `images/`, and `config/theme.properties` untouched. Existing data files seed only when absent in the existing repository classes.

TEST STATUS
-----------
These are generated staging contents, not verified build outputs. No compile, HTTP behavior test, or Windows package run has been performed by creating these files. Follow the workflow/local instructions after manually applying them. Do not copy test data into the real data directory.
