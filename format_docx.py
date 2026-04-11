"""
Format the 603_MAD_Project_Report_v3.before_format_update.docx:
- Font: Times New Roman, size 12 (headings 14)
- Paragraph spacing: 1.5 line spacing
- Header: "Deskify — Workspace Booking App" on each page
- Footer: Page number on each page
- Fill index page numbers
- Increase table column widths
- Mention Dhananjay Patel sir in acknowledgement
"""

from docx import Document
from docx.shared import Pt, Inches, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml
import copy

INPUT_FILE = '603_MAD_Project_Report_v3.before_format_update.docx'
OUTPUT_FILE = '603_MAD_Project_Report_v3_formatted.docx'

doc = Document(INPUT_FILE)

# ============================================================
# 1. Identify heading paragraphs (section titles)
# ============================================================
heading_keywords = [
    "PROJECT REPORT", "COLLEGE CERTIFICATE", "ACKNOWLEDGEMENT", "INDEX",
    "1. Introduction to Project",
    "2. Technology Used",
    "3. Objectives",
    "4. System Flow Chart",
    "5. Database Design",
    "6. Screenshots",
    "7. References",
]

sub_heading_keywords = [
    "2.1 ", "2.2 ", "2.3 ",
    "4.1 ", "4.2 ", "4.3 ",
    "6.1 ", "6.2 ",
    "Collection 1:", "Collection 2:", "Collection 3:", "Collection 4:",
    "Collection Relationships", "Supabase Storage Structure",
    "User Side Navigation:", "Admin Side Navigation:",
]

def is_heading(text):
    text = text.strip()
    for kw in heading_keywords:
        if text.startswith(kw):
            return True
    return False

def is_sub_heading(text):
    text = text.strip()
    for kw in sub_heading_keywords:
        if text.startswith(kw):
            return True
    return False

# ============================================================
# 2. Apply font and spacing to all paragraphs
# ============================================================
for para in doc.paragraphs:
    text = para.text.strip()
    if not text:
        continue

    # Set line spacing to 1.5
    pf = para.paragraph_format
    pf.line_spacing = 1.5

    # Determine font size
    if is_heading(text):
        font_size = Pt(14)
    elif is_sub_heading(text):
        font_size = Pt(13)
    else:
        font_size = Pt(12)

    # Apply font to all runs
    for run in para.runs:
        run.font.name = 'Times New Roman'
        run.font.size = font_size
        # Set font for East Asian text too
        r = run._element
        rPr = r.find(qn('w:rPr'))
        if rPr is None:
            rPr = parse_xml('<w:rPr %s/>' % nsdecls('w'))
            r.insert(0, rPr)
        rFonts = rPr.find(qn('w:rFonts'))
        if rFonts is None:
            rFonts = parse_xml('<w:rFonts %s/>' % nsdecls('w'))
            rPr.insert(0, rFonts)
        rFonts.set(qn('w:ascii'), 'Times New Roman')
        rFonts.set(qn('w:hAnsi'), 'Times New Roman')
        rFonts.set(qn('w:cs'), 'Times New Roman')

    # Make headings bold
    if is_heading(text):
        for run in para.runs:
            run.bold = True

print("Applied fonts and spacing to all paragraphs.")

# ============================================================
# 3. Apply font to all table cells
# ============================================================
for table in doc.tables:
    for row in table.rows:
        for cell in row.cells:
            for para in cell.paragraphs:
                pf = para.paragraph_format
                pf.line_spacing = 1.15  # slightly tighter in tables
                for run in para.runs:
                    run.font.name = 'Times New Roman'
                    run.font.size = Pt(11)
                    r = run._element
                    rPr = r.find(qn('w:rPr'))
                    if rPr is None:
                        rPr = parse_xml('<w:rPr %s/>' % nsdecls('w'))
                        r.insert(0, rPr)
                    rFonts = rPr.find(qn('w:rFonts'))
                    if rFonts is None:
                        rFonts = parse_xml('<w:rFonts %s/>' % nsdecls('w'))
                        rPr.insert(0, rFonts)
                    rFonts.set(qn('w:ascii'), 'Times New Roman')
                    rFonts.set(qn('w:hAnsi'), 'Times New Roman')
                    rFonts.set(qn('w:cs'), 'Times New Roman')

    # Make header row bold
    if len(table.rows) > 0:
        for cell in table.rows[0].cells:
            for para in cell.paragraphs:
                for run in para.runs:
                    run.bold = True

print("Applied fonts to all table cells.")

# ============================================================
# 4. Increase table widths (use full page width)
# ============================================================
for table in doc.tables:
    tbl = table._tbl
    tblPr = tbl.find(qn('w:tblPr'))
    if tblPr is None:
        tblPr = parse_xml('<w:tblPr %s/>' % nsdecls('w'))
        tbl.insert(0, tblPr)

    # Set table width to 100% (5000 = 100% in pct units)
    tblW = tblPr.find(qn('w:tblW'))
    if tblW is None:
        tblW = parse_xml('<w:tblW %s w:w="5000" w:type="pct"/>' % nsdecls('w'))
        tblPr.append(tblW)
    else:
        tblW.set(qn('w:w'), '5000')
        tblW.set(qn('w:type'), 'pct')

print("Set all tables to full page width.")

# ============================================================
# 5. Update acknowledgement to mention Dhananjay Patel sir
# ============================================================
for i, para in enumerate(doc.paragraphs):
    if para.text.strip().startswith("We would like to express our sincere gratitude"):
        # Replace "our project guide" with named mention
        old_text = para.text
        new_text = old_text.replace(
            "we thank our project guide for their continuous guidance",
            "we thank our project guide, Prof. Dhananjay Patel Sir, for his continuous guidance"
        )
        if new_text != old_text:
            # Clear existing runs and set new text
            for run in para.runs:
                run.text = ""
            para.runs[0].text = new_text
            para.runs[0].font.name = 'Times New Roman'
            para.runs[0].font.size = Pt(12)
            print(f"Updated acknowledgement at paragraph {i}.")
        break

# ============================================================
# 6. Fill INDEX page numbers (approximate mapping)
# ============================================================
# We'll assign estimated page numbers based on document structure
# Title page = 1, Certificate = 2, Acknowledgement = 3, Index = 4
# Content starts from page 5
page_map = {
    'Introduction to Project': '5',
    'Technology Used': '6',
    '2.1  Frontend (Android)': '6',
    '2.2  Backend (Firebase + Supabase)': '7',
    '2.3  Development Environment': '7',
    'Objectives': '8',
    'System Flow Chart / Site Diagram': '9',
    'Database Design': '11',
    'Screenshots (Input, Output)': '14',
    '6.1  Input Screens': '14',
    '6.2  Output Screens': '18',
    'References': '21',
}

# Table 1 is the INDEX table
index_table = doc.tables[1]
for row in index_table.rows[1:]:  # skip header
    desc = row.cells[1].text.strip()
    if desc in page_map:
        # Set page number
        cell = row.cells[2]
        cell.text = page_map[desc]
        for para in cell.paragraphs:
            para.alignment = WD_ALIGN_PARAGRAPH.CENTER
            pf = para.paragraph_format
            pf.line_spacing = 1.5
            for run in para.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(12)

print("Filled index page numbers.")

# ============================================================
# 7. Add Header and Footer
# ============================================================
for section in doc.sections:
    # --- HEADER ---
    section.header.is_linked_to_previous = False
    header = section.header
    # Clear existing
    for p in header.paragraphs:
        p.clear()

    hp = header.paragraphs[0]
    hp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = hp.add_run("Deskify \u2014 Workspace Booking App")
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10)
    run.italic = True
    run.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

    # Add a bottom border to the header paragraph
    pPr = hp._element.get_or_add_pPr()
    pBdr = parse_xml(
        '<w:pBdr %s>'
        '  <w:bottom w:val="single" w:sz="4" w:space="1" w:color="999999"/>'
        '</w:pBdr>' % nsdecls('w')
    )
    pPr.append(pBdr)

    # --- FOOTER ---
    section.footer.is_linked_to_previous = False
    footer = section.footer
    for p in footer.paragraphs:
        p.clear()

    fp = footer.paragraphs[0]
    fp.alignment = WD_ALIGN_PARAGRAPH.CENTER

    # Add page number field
    run = fp.add_run("Page ")
    run.font.name = 'Times New Roman'
    run.font.size = Pt(10)
    run.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

    # PAGE field code
    fldChar1 = parse_xml('<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
    instrText = parse_xml('<w:instrText %s xml:space="preserve"> PAGE </w:instrText>' % nsdecls('w'))
    fldChar2 = parse_xml('<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))

    run2 = fp.add_run()
    run2._element.append(fldChar1)
    run3 = fp.add_run()
    run3._element.append(instrText)
    run4 = fp.add_run()
    run4._element.append(fldChar2)

    # Add " of NUMPAGES"
    run5 = fp.add_run(" of ")
    run5.font.name = 'Times New Roman'
    run5.font.size = Pt(10)
    run5.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

    fldChar3 = parse_xml('<w:fldChar %s w:fldCharType="begin"/>' % nsdecls('w'))
    instrText2 = parse_xml('<w:instrText %s xml:space="preserve"> NUMPAGES </w:instrText>' % nsdecls('w'))
    fldChar4 = parse_xml('<w:fldChar %s w:fldCharType="end"/>' % nsdecls('w'))

    run6 = fp.add_run()
    run6._element.append(fldChar3)
    run7 = fp.add_run()
    run7._element.append(instrText2)
    run8 = fp.add_run()
    run8._element.append(fldChar4)

    # Add top border to footer
    pPr2 = fp._element.get_or_add_pPr()
    pBdr2 = parse_xml(
        '<w:pBdr %s>'
        '  <w:top w:val="single" w:sz="4" w:space="1" w:color="999999"/>'
        '</w:pBdr>' % nsdecls('w')
    )
    pPr2.append(pBdr2)

print("Added header and footer with title and page numbers.")

# ============================================================
# 8. Save
# ============================================================
doc.save(OUTPUT_FILE)
print(f"\nDone! Saved to: {OUTPUT_FILE}")
