import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from docx import Document
from docx.shared import Pt, Inches, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.section import WD_ORIENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml

doc = Document()

# ========== PAGE SETUP ==========
for section in doc.sections:
    section.top_margin = Cm(2.54)
    section.bottom_margin = Cm(2.54)
    section.left_margin = Cm(2.54)
    section.right_margin = Cm(2.54)

style = doc.styles['Normal']
font = style.font
font.name = 'Times New Roman'
font.size = Pt(12)

# Helper functions
def set_cell_shading(cell, color):
    shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color}"/>')
    cell._tc.get_or_add_tcPr().append(shading)

def add_formatted_paragraph(doc, text, size=12, bold=False, alignment=WD_ALIGN_PARAGRAPH.LEFT, space_before=0, space_after=6, color=None):
    p = doc.add_paragraph()
    p.alignment = alignment
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    run = p.add_run(text)
    run.font.size = Pt(size)
    run.font.name = 'Times New Roman'
    run.bold = bold
    if color:
        run.font.color.rgb = RGBColor(*color)
    return p

def add_table_with_style(doc, headers, rows, col_widths=None):
    table = doc.add_table(rows=1 + len(rows), cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.style = 'Table Grid'

    # Header row
    for i, header in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = ''
        p = cell.paragraphs[0]
        run = p.add_run(header)
        run.bold = True
        run.font.size = Pt(11)
        run.font.name = 'Times New Roman'
        set_cell_shading(cell, "D9E2F3")

    # Data rows
    for r, row_data in enumerate(rows):
        for c, cell_text in enumerate(row_data):
            cell = table.rows[r + 1].cells[c]
            cell.text = ''
            p = cell.paragraphs[0]
            run = p.add_run(str(cell_text))
            run.font.size = Pt(11)
            run.font.name = 'Times New Roman'

    if col_widths:
        for i, width in enumerate(col_widths):
            for row in table.rows:
                row.cells[i].width = Inches(width)

    doc.add_paragraph()  # spacing after table
    return table


# ================================================================
# COVER PAGE
# ================================================================
doc.add_paragraph()  # top spacing

add_formatted_paragraph(doc, "A", size=14, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=30)
add_formatted_paragraph(doc, "PROJECT REPORT", size=18, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=6)
add_formatted_paragraph(doc, "ON", size=14, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=6)
add_formatted_paragraph(doc, '"DESKIFY \u2014 WORKSPACE BOOKING APP"', size=16, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12)

doc.add_paragraph()
add_formatted_paragraph(doc, "As a Partial Requirement for the Degree of", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12)
add_formatted_paragraph(doc, "BACHELOR OF COMPUTER APPLICATION", size=14, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER)
add_formatted_paragraph(doc, "(B.C.A.)", size=13, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER)

add_formatted_paragraph(doc, "Submitted to", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=18)

doc.add_paragraph()
add_formatted_paragraph(doc, "[ College Logo Here ]", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=6, color=(128, 128, 128))
doc.add_paragraph()

add_formatted_paragraph(doc, "C.B. PATEL COMPUTER COLLEGE &", size=14, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=6)
add_formatted_paragraph(doc, "J.N.M. PATEL SCIENCE COLLEGE,", size=14, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER)
add_formatted_paragraph(doc, "BHARTHANA, VESU, SURAT", size=13, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER)

add_formatted_paragraph(doc, "Affiliated to", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12)
add_formatted_paragraph(doc, "VEER NARMAD SOUTH GUJARAT UNIVERSITY, SURAT.", size=13, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER)

add_formatted_paragraph(doc, "ACADEMIC YEAR: 2025-2026", size=13, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=18)

doc.add_paragraph()

# Guide and Students table on cover page
guide_table = doc.add_table(rows=1, cols=2)
guide_table.alignment = WD_TABLE_ALIGNMENT.CENTER
cell_left = guide_table.rows[0].cells[0]
cell_right = guide_table.rows[0].cells[1]

p_left = cell_left.paragraphs[0]
p_left.alignment = WD_ALIGN_PARAGRAPH.LEFT
run_l = p_left.add_run("Guided by:\n____________________\n(Faculty Name)")
run_l.font.size = Pt(11)
run_l.font.name = 'Times New Roman'

p_right = cell_right.paragraphs[0]
p_right.alignment = WD_ALIGN_PARAGRAPH.LEFT
run_r = p_right.add_run("Submitted by:\nStudent Name (Exam No. XXXX)\nStudent Name (Exam No. XXXX)\nStudent Name (Exam No. XXXX)")
run_r.font.size = Pt(11)
run_r.font.name = 'Times New Roman'

# Remove table borders
for row in guide_table.rows:
    for cell in row.cells:
        tc = cell._tc
        tcPr = tc.get_or_add_tcPr()
        tcBorders = parse_xml(
            f'<w:tcBorders {nsdecls("w")}>'
            '<w:top w:val="none" w:sz="0" w:space="0" w:color="auto"/>'
            '<w:left w:val="none" w:sz="0" w:space="0" w:color="auto"/>'
            '<w:bottom w:val="none" w:sz="0" w:space="0" w:color="auto"/>'
            '<w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/>'
            '</w:tcBorders>'
        )
        tcPr.append(tcBorders)


# ================================================================
# PAGE BREAK - CERTIFICATE
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "COLLEGE CERTIFICATE", size=16, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=30)
doc.add_paragraph()
add_formatted_paragraph(doc, "[ College Certificate will be placed here after project completion ]", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=60, color=(128, 128, 128))


# ================================================================
# PAGE BREAK - ACKNOWLEDGEMENT
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "ACKNOWLEDGEMENT", size=16, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12)
doc.add_paragraph()

ack1 = "We would like to express our sincere gratitude to all those who helped us throughout this project. First, we thank our project guide for their continuous guidance and support during the development of this project. Their suggestions helped us stay on the right track and complete the work on time."
ack2 = "We are also grateful to C.B. Patel Computer College and the Department of Computer Application for giving us the opportunity to work on a practical project as part of our BCA curriculum. The college lab facilities and resources were helpful during the development phase."
ack3 = "Lastly, we thank our family and friends who supported us throughout this semester. This project gave us a chance to apply what we learned in classrooms to a real-world problem, and we are thankful for that experience."

for ack in [ack1, ack2, ack3]:
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.line_spacing = Pt(18)
    run = p.add_run(ack)
    run.font.size = Pt(12)
    run.font.name = 'Times New Roman'

doc.add_paragraph()
add_formatted_paragraph(doc, "\u2014 Students", size=12, bold=True, alignment=WD_ALIGN_PARAGRAPH.RIGHT, space_before=24)


# ================================================================
# PAGE BREAK - INDEX
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "INDEX", size=16, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12)
doc.add_paragraph()

index_data = [
    ("1.", "Introduction to Project", ""),
    ("2.", "Technology Used", ""),
    ("", "   2.1  Frontend (Android)", ""),
    ("", "   2.2  Backend (Firebase + Supabase)", ""),
    ("", "   2.3  Development Environment", ""),
    ("3.", "Objectives", ""),
    ("4.", "System Flow Chart / Site Diagram", ""),
    ("5.", "Database Design", ""),
    ("6.", "Screenshots (Input, Output)", ""),
    ("", "   6.1  Input Screens", ""),
    ("", "   6.2  Output Screens", ""),
    ("7.", "References", ""),
]

add_table_with_style(doc, ["SR NO.", "DESCRIPTION", "PAGE NO."], index_data, col_widths=[0.8, 4.5, 1.0])


# ================================================================
# 1. INTRODUCTION TO PROJECT
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "1. Introduction to Project", size=16, bold=True, space_before=12, space_after=12)

intro1 = "Co-working spaces are growing fast in cities like Surat. Most of them offer daily trial passes or monthly memberships, but if someone wants to book a specific room for a specific time \u2014 say a meeting room for 2 hours on a Thursday \u2014 there is no simple way to do it. The booking process usually involves calling the workspace, checking availability over WhatsApp, and confirming manually. For workspace owners managing multiple rooms with different capacities and equipment, tracking who booked what and when becomes a spreadsheet problem very quickly."

intro2 = 'Deskify is an Android application built to solve this. It lets workspace owners list their rooms with details like capacity, location, available equipment (computer, projector), and photos. Users can browse rooms, filter by type or amenities, view detailed room information, and book a specific room for a specific date and time slot. The admin side handles room management \u2014 add, edit, delete rooms, upload images, customize the workspace banner, and view all bookings per room with user details. Authentication is role-based: admins see the management panel, regular users see the booking interface. The backend runs entirely on Firebase (Authentication and Cloud Firestore) with Supabase handling image storage \u2014 keeping the app serverless with zero infrastructure to maintain.'

for text in [intro1, intro2]:
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.line_spacing = Pt(18)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    run = p.add_run(text)
    run.font.size = Pt(12)
    run.font.name = 'Times New Roman'


# ================================================================
# 2. TECHNOLOGY USED
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "2. Technology Used", size=16, bold=True, space_before=12, space_after=12)

# 2.1 Frontend
add_formatted_paragraph(doc, "2.1 Frontend (Android)", size=14, bold=True, space_before=12, space_after=8)

frontend_rows = [
    ("Kotlin", "2.0.21", "Primary programming language for the Android application"),
    ("Android SDK", "API 35 (min 27)", "Target and minimum Android platform versions"),
    ("Material Design 3", "1.12.0", "UI component library for buttons, cards, dialogs, and navigation"),
    ("ViewPager2", "Latest", "Image slider for room photo galleries on details screen"),
    ("RecyclerView", "Latest", "Efficient scrollable lists for rooms and bookings display"),
    ("Coil", "2.6.0", "Image loading library for displaying room photos from URLs"),
    ("ConstraintLayout", "2.2.1", "Flexible layout system for complex screen designs"),
    ("ViewBinding", "Enabled", "Type-safe view access without findViewById in activities"),
    ("Android Studio", "Latest", "IDE used for development, debugging, and APK generation"),
]
add_table_with_style(doc, ["Technology", "Version", "Purpose"], frontend_rows, col_widths=[1.5, 1.2, 3.6])

# 2.2 Backend
add_formatted_paragraph(doc, "2.2 Backend (Firebase + Supabase)", size=14, bold=True, space_before=12, space_after=8)

backend_rows = [
    ("Firebase Authentication", "24.0.1", "Email/password login and registration with session management"),
    ("Cloud Firestore", "25.0.0", "NoSQL database storing rooms, bookings, users, and workspace data"),
    ("Supabase Storage", "REST API", "Image hosting for room photos and workspace banners"),
    ("OkHttp", "4.12.0", "HTTP client for uploading and deleting images on Supabase"),
    ("Google Services Plugin", "4.4.2", "Connects the Android app to the Firebase project"),
]
add_table_with_style(doc, ["Technology", "Version", "Purpose"], backend_rows, col_widths=[1.8, 1.0, 3.5])

# 2.3 Development Environment
add_formatted_paragraph(doc, "2.3 Development Environment", size=14, bold=True, space_before=12, space_after=8)

dev_rows = [
    ("Operating System", "Windows 11"),
    ("IDE", "Android Studio (Latest)"),
    ("Language", "Kotlin 2.0.21"),
    ("Build System", "Gradle with Kotlin DSL and Version Catalog"),
    ("Version Control", "Git + GitHub"),
    ("Testing", "Android Emulator / Physical Device (API 27+)"),
    ("Database Console", "Firebase Console (console.firebase.google.com)"),
    ("Storage Console", "Supabase Dashboard (app.supabase.com)"),
]
add_table_with_style(doc, ["Component", "Detail"], dev_rows, col_widths=[2.0, 4.3])


# ================================================================
# 3. OBJECTIVES
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "3. Objectives", size=16, bold=True, space_before=12, space_after=12)

obj1 = "The idea behind Deskify started as a college project but grew from a real observation \u2014 co-working spaces in Surat sell monthly passes or day passes, but there is no easy way for someone to book a specific room for a specific time. If a freelancer needs a meeting room with a projector for 2 hours, or a small team wants a quiet room with computers for an afternoon, they have to call and negotiate. The current project does not cover payments or real-time availability sync yet, but it builds the core booking flow that such a system would need."

obj2 = "The objectives of this application are: (1) Allow workspace admins to register rooms with full details \u2014 name, type (meeting, normal, chill), capacity, location, description, equipment availability (computer, projector), and multiple photos uploaded to cloud storage. (2) Let users browse all available rooms with a search bar and advanced filters \u2014 filter by room type, location, capacity range, and equipment. (3) Provide a detailed room view with an image gallery, feature badges, and a one-tap booking flow where the user picks a date, start time, end time, and optionally adds a purpose. (4) Show users their booking history on the profile page with status tracking (upcoming, completed, cancelled) and the ability to cancel confirmed bookings. (5) Give admins a dashboard to manage rooms (add, edit, delete), customize the workspace name and banner, and view all bookings for any room with user details. (6) Implement role-based access \u2014 the splash screen checks Firebase Auth and routes admins to the admin panel and regular users to the booking interface automatically. (7) Use Firebase for authentication and data, Supabase for image storage \u2014 keeping the app fully serverless with no backend server to deploy or maintain."

for text in [obj1, obj2]:
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.line_spacing = Pt(18)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    run = p.add_run(text)
    run.font.size = Pt(12)
    run.font.name = 'Times New Roman'


# ================================================================
# 4. SYSTEM FLOW CHART / SITE DIAGRAM
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "4. System Flow Chart / Site Diagram", size=16, bold=True, space_before=12, space_after=12)

add_formatted_paragraph(doc, "4.1 Application Navigation Flow", size=14, bold=True, space_before=12, space_after=8)

add_formatted_paragraph(doc, "[ Flow Chart Diagram will be placed here ]", size=12, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=30, space_after=6, color=(128, 128, 128))
add_formatted_paragraph(doc, "Figure 4.1 \u2014 Application Navigation Flow Chart", size=11, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=6, space_after=24)

# Request flow text
add_formatted_paragraph(doc, "4.2 How a Booking Works", size=14, bold=True, space_before=18, space_after=8)

flow_text = 'User opens app \u2192 Splash screen checks Firebase Auth \u2192 routes to Home screen \u2192 user browses or filters rooms \u2192 taps a room card \u2192 sees room details with photos, features, and description \u2192 taps "Book This Room" \u2192 picks date, start time, end time \u2192 confirms booking \u2192 booking saved to Cloud Firestore \u2192 appears in Profile page with "Upcoming" status.'

p = doc.add_paragraph()
p.paragraph_format.space_after = Pt(12)
p.paragraph_format.line_spacing = Pt(18)
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
run = p.add_run(flow_text)
run.font.size = Pt(12)
run.font.name = 'Times New Roman'

# Navigation structure tables
add_formatted_paragraph(doc, "4.3 Screen Navigation Structure", size=14, bold=True, space_before=18, space_after=8)

add_formatted_paragraph(doc, "User Side Navigation:", size=12, bold=True, space_before=8, space_after=6)

user_nav = [
    ("Splash Screen", "\u2192", "Login / Home (auto-login check)"),
    ("Login Screen", "\u2192", "Home Screen or Register Screen"),
    ("Register Screen", "\u2192", "Login Screen (after successful registration)"),
    ("Home Screen", "\u2192", "Room Details Screen (tap room card)"),
    ("Home Screen", "\u2192", "Filter Dialog (tap filter button)"),
    ("Room Details Screen", "\u2192", "Booking Screen (tap Book This Room)"),
    ("Booking Screen", "\u2192", "Profile Screen (after successful booking)"),
    ("Profile Screen", "\u2192", "Cancel Booking / Logout"),
]
add_table_with_style(doc, ["From Screen", "", "To Screen"], user_nav, col_widths=[2.0, 0.5, 3.8])

add_formatted_paragraph(doc, "Admin Side Navigation:", size=12, bold=True, space_before=12, space_after=6)

admin_nav = [
    ("Splash Screen", "\u2192", "Login / Admin Dashboard (auto-login check)"),
    ("Admin Dashboard", "\u2192", "Add Room Dialog (tap + button)"),
    ("Admin Dashboard", "\u2192", "Edit Room Dialog (tap Edit on room)"),
    ("Admin Dashboard", "\u2192", "Room Bookings Dialog (tap Bookings on room)"),
    ("Admin Dashboard", "\u2192", "Workspace Settings Dialog (tap Settings)"),
    ("Admin Dashboard", "\u2192", "Delete Confirmation (tap Delete on room)"),
    ("Admin Dashboard", "\u2192", "Login Screen (Logout)"),
]
add_table_with_style(doc, ["From Screen", "", "To Screen"], admin_nav, col_widths=[2.0, 0.5, 3.8])


# ================================================================
# 5. DATABASE DESIGN
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "5. Database Design", size=16, bold=True, space_before=12, space_after=12)

db_intro = "The application uses Cloud Firestore, a NoSQL document database by Google Firebase. Data is organized into four top-level collections. Each document has an auto-generated ID (except the workspaces collection which uses a fixed \"default\" document). Image files are stored separately on Supabase Storage and referenced by URL in Firestore documents."

p = doc.add_paragraph()
p.paragraph_format.space_after = Pt(12)
p.paragraph_format.line_spacing = Pt(18)
p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
run = p.add_run(db_intro)
run.font.size = Pt(12)
run.font.name = 'Times New Roman'

# Collection 1: users
add_formatted_paragraph(doc, "Collection 1: users", size=13, bold=True, space_before=14, space_after=6)

users_rows = [
    ("(document ID)", "String", "Firebase Auth UID (auto-assigned)"),
    ("name", "String", "User's full name entered during registration"),
    ("email", "String", "Login email address"),
    ("role", "String", '"user" or "admin" \u2014 determines which screen they see after login'),
    ("createdAt", "Number", "Timestamp in milliseconds when account was created"),
]
add_table_with_style(doc, ["Field", "Data Type", "Description"], users_rows, col_widths=[1.5, 1.0, 3.8])

# Collection 2: rooms
add_formatted_paragraph(doc, "Collection 2: rooms", size=13, bold=True, space_before=14, space_after=6)

rooms_rows = [
    ("(document ID)", "String", "Auto-generated Firestore document ID"),
    ("name", "String", 'Room display name (e.g., "Conference Room A")'),
    ("roomType", "String", 'Category: "meeting", "normal", or "chill"'),
    ("size", "Number", "Maximum capacity in number of people"),
    ("location", "String", 'Floor or area description (e.g., "2nd Floor")'),
    ("hasComputer", "Boolean", "Whether the room has a computer"),
    ("hasProjector", "Boolean", "Whether the room has a projector"),
    ("description", "String", "Detailed room description shown on details page"),
    ("imageUrls", "Array<String>", "Supabase public URLs for room photos"),
    ("workspaceId", "String", "Reserved for future multi-workspace support"),
    ("createdAt", "Number", "Timestamp in milliseconds"),
]
add_table_with_style(doc, ["Field", "Data Type", "Description"], rooms_rows, col_widths=[1.5, 1.2, 3.6])

# Collection 3: bookings
doc.add_page_break()
add_formatted_paragraph(doc, "Collection 3: bookings", size=13, bold=True, space_before=14, space_after=6)

bookings_rows = [
    ("(document ID)", "String", "Auto-generated Firestore document ID"),
    ("roomId", "String", "References the room document ID"),
    ("roomName", "String", "Denormalized room name for quick display without extra query"),
    ("userId", "String", "Firebase Auth UID of the user who booked"),
    ("userEmail", "String", "Denormalized email for admin booking view"),
    ("date", "String", 'Booking date in "yyyy-MM-dd" format'),
    ("time", "String", 'Time range in "HH:mm - HH:mm" format'),
    ("status", "String", '"confirmed", "completed", or "cancelled"'),
    ("createdAt", "Number", "Timestamp in milliseconds"),
]
add_table_with_style(doc, ["Field", "Data Type", "Description"], bookings_rows, col_widths=[1.5, 1.0, 3.8])

# Collection 4: workspaces
add_formatted_paragraph(doc, "Collection 4: workspaces", size=13, bold=True, space_before=14, space_after=6)

workspaces_rows = [
    ("(document ID)", "String", 'Fixed value: "default"'),
    ("name", "String", "Workspace display name shown on home screen header"),
    ("bannerUrl", "String", "Supabase public URL for the workspace banner image"),
    ("createdAt", "Number", "Timestamp in milliseconds"),
]
add_table_with_style(doc, ["Field", "Data Type", "Description"], workspaces_rows, col_widths=[1.5, 1.0, 3.8])

# Collection Relationships
add_formatted_paragraph(doc, "Collection Relationships", size=13, bold=True, space_before=14, space_after=6)

rel_rows = [
    ("users \u2192 bookings", "bookings.userId matches users document ID (Firebase UID)"),
    ("rooms \u2192 bookings", "bookings.roomId matches rooms document ID"),
    ("workspaces \u2192 rooms", "rooms.workspaceId references workspace (single workspace currently)"),
]
add_table_with_style(doc, ["Relationship", "How It Works"], rel_rows, col_widths=[2.0, 4.3])

# Supabase Storage
add_formatted_paragraph(doc, "Supabase Storage Structure", size=13, bold=True, space_before=14, space_after=6)

storage_rows = [
    ("room-images", "rooms/", "Room photos uploaded by admin (multiple per room)"),
    ("room-images", "banners/", "Workspace banner images (one active at a time)"),
]
add_table_with_style(doc, ["Bucket", "Folder", "Purpose"], storage_rows, col_widths=[1.5, 1.3, 3.5])


# ================================================================
# 6. SCREENSHOTS
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "6. Screenshots (Input, Output)", size=16, bold=True, space_before=12, space_after=12)

add_formatted_paragraph(doc, "6.1 Input Screens", size=14, bold=True, space_before=12, space_after=8)

input_screenshots = [
    ("Figure 6.1", "Splash Screen", "App splash screen with logo and 2-second auto-redirect to login or home"),
    ("Figure 6.2", "Login Screen", "Email and password fields with Sign In button and Sign Up link at bottom"),
    ("Figure 6.3", "Registration Screen", "Full name, email, password, confirm password fields with Register button"),
    ("Figure 6.4", "Add Room Dialog (Admin)", "Room name, type dropdown, capacity, location, description, computer/projector toggles, image upload area, save button"),
    ("Figure 6.5", "Edit Room Dialog (Admin)", "Pre-filled room details with existing images shown, option to add or remove images"),
    ("Figure 6.6", "Workspace Settings Dialog (Admin)", "Workspace name field, current banner preview, change banner button, save button"),
    ("Figure 6.7", "Filter Dialog", "Room type dropdown, location dropdown, capacity min/max inputs, computer and projector toggles, reset and apply buttons"),
    ("Figure 6.8", "Booking Screen", "Date picker button, start time spinner, end time spinner, auto-calculated duration display, purpose field, confirm button"),
]

for fig_num, title, desc in input_screenshots:
    doc.add_paragraph()
    add_formatted_paragraph(doc, f"{title}", size=12, bold=True, space_before=12, space_after=4)
    add_formatted_paragraph(doc, desc, size=11, space_before=2, space_after=4, color=(80, 80, 80))
    add_formatted_paragraph(doc, f"[ Screenshot will be placed here ]", size=11, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12, space_after=4, color=(128, 128, 128))
    add_formatted_paragraph(doc, f"{fig_num} \u2014 {title}", size=11, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=4, space_after=12)

doc.add_page_break()
add_formatted_paragraph(doc, "6.2 Output Screens", size=14, bold=True, space_before=12, space_after=8)

output_screenshots = [
    ("Figure 6.9", "Home Screen (User)", "Search bar at top, workspace banner with name, room cards showing image, type badge, capacity, location, and feature icons"),
    ("Figure 6.10", "Room Details Screen", "Image slider at top, room name and type badge, capacity and location info cards, computer/projector feature cards, description section, Book This Room button"),
    ("Figure 6.11", "Profile Screen with Bookings", "User name and email at top, list of booking cards showing room name, formatted date, time, status badge (Upcoming/Completed/Cancelled), cancel button for upcoming bookings"),
    ("Figure 6.12", "Admin Dashboard", "Workspace banner and name at top, scrollable room list with edit/delete/bookings buttons per room, floating add button, settings and logout buttons"),
    ("Figure 6.13", "Room Bookings Dialog (Admin)", "Dialog showing all bookings for a specific room with user email, formatted date, time, and color-coded status badge for each booking"),
    ("Figure 6.14", "Search and Filter Results", "Home screen after applying filters showing only matching rooms, or empty state if no rooms match"),
]

for fig_num, title, desc in output_screenshots:
    doc.add_paragraph()
    add_formatted_paragraph(doc, f"{title}", size=12, bold=True, space_before=12, space_after=4)
    add_formatted_paragraph(doc, desc, size=11, space_before=2, space_after=4, color=(80, 80, 80))
    add_formatted_paragraph(doc, f"[ Screenshot will be placed here ]", size=11, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=12, space_after=4, color=(128, 128, 128))
    add_formatted_paragraph(doc, f"{fig_num} \u2014 {title}", size=11, bold=True, alignment=WD_ALIGN_PARAGRAPH.CENTER, space_before=4, space_after=12)


# ================================================================
# 7. REFERENCES
# ================================================================
doc.add_page_break()
add_formatted_paragraph(doc, "7. References", size=16, bold=True, space_before=12, space_after=12)

references = [
    "Android Developer Documentation \u2014 developer.android.com",
    "Kotlin Official Documentation \u2014 kotlinlang.org/docs",
    "Firebase Authentication Guide \u2014 firebase.google.com/docs/auth",
    "Cloud Firestore Documentation \u2014 firebase.google.com/docs/firestore",
    "Supabase Storage Documentation \u2014 supabase.com/docs/guides/storage",
    "Material Design 3 Components \u2014 m3.material.io",
    "Coil Image Loading Library \u2014 coil-kt.github.io/coil",
    "OkHttp Documentation \u2014 square.github.io/okhttp",
    "Claude AI \u2014 used as a development and debugging assistant during the project",
]

for i, ref in enumerate(references, 1):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = Pt(18)
    run = p.add_run(f"{i}. {ref}")
    run.font.size = Pt(12)
    run.font.name = 'Times New Roman'


# ================================================================
# SAVE
# ================================================================
output_path = r"c:\Users\ayaz soni\AndroidStudioProjects\WORKSPACE_BOOKING_APP\603_MAD_Project_Report.docx"
doc.save(output_path)
print(f"Document saved to: {output_path}")
