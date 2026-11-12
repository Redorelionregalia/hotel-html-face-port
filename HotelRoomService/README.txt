THE GRAND AURELIA - ROOM SERVICE (Customer app + Staff app)
===========================================================

FOLDER (keep everything together in this one folder)
  Customer.exe / Customer.bat   guest app
  Staff.exe    / Staff.bat      staff app   (login: staff / staff123)
  HotelApp.jar                  the program (both apps are inside it)
  config\theme.properties       EDIT THIS: colors, text colors, fonts, hotel name, texts, staff login
  images\                       item pictures
  data\                         menu.txt + orders.txt (created on first run, shared by both apps)
  src\hotel\...                 the Java source code (packages: model, data, ui, app)
  OOP_GUIDE.txt                 explains where and how OOP is used (for the school project)

RUNNING
  Needs Java 11 or newer (free: https://adoptium.net).
  Double-click Customer.bat and Staff.bat, or the .exe files once you have built them.
  Open both at the same time: orders placed in Customer appear in Staff within ~3 seconds,
  and price/stock edits in Staff appear in Customer automatically.

MAKING THE .EXE FILES (Windows)
  1. Install Launch4j (free): https://launch4j.sourceforge.net
  2. Double-click build_exe.bat  ->  Customer.exe and Staff.exe appear in this folder.
  (If you change any .java file, double-click build_jar.bat first. It needs a JDK.)

ITEM PICTURES
  - Staff app > Menu & Stock > "Photo..." copies the picture into the images folder.
  - Or drop a file into images\ named after the item with dashes:
      club-sandwich.png, bottled-water.jpg, house-red-wine.png ...
  - No picture = the emoji is shown instead.

CHANGING COLORS / TEXT
  Open config\theme.properties in Notepad, change a value (for example color.accent=#C0392B),
  save, then close and reopen the apps.

RESET ALL DATA
  Close both apps and delete the data folder's menu.txt and orders.txt. They are recreated.
