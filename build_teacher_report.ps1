$ErrorActionPreference = "Stop"

$wdAlignParagraphLeft = 0
$wdAlignParagraphCenter = 1
$wdAlignParagraphJustify = 3
$wdHeaderFooterPrimary = 1
$wdHeaderFooterFirstPage = 2
$wdLineSpace1pt5 = 1
$wdRowHeightAtLeast = 1
$wdStatisticPages = 2
$wdFieldEmpty = -1

$workdir = Get-Location
$source = Join-Path $workdir "603_MAD_Project_Report_v3.before_format_update.docx"
if (-not (Test-Path $source)) {
    $source = Join-Path $workdir "603_MAD_Project_Report_v3.docx"
}
$target = Join-Path $workdir "603_MAD_Project_Report_v4_teacher.docx"

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0

function Clean-Text($text) {
    return (($text -replace "[`r`a]", "") -replace "\s+", " ").Trim()
}

try {
    $doc = $word.Documents.Open($source)

    $doc.SaveAs([string]$target)

    $doc.PageSetup.TopMargin = $word.CentimetersToPoints(2.54)
    $doc.PageSetup.BottomMargin = $word.CentimetersToPoints(2.54)
    $doc.PageSetup.LeftMargin = $word.CentimetersToPoints(2.54)
    $doc.PageSetup.RightMargin = $word.CentimetersToPoints(2.54)

    foreach ($styleName in @("Normal", "Heading 1", "Heading 2", "TOC 1", "TOC 2")) {
        $style = $doc.Styles.Item($styleName)
        $style.Font.Name = "Times New Roman"
        if ($styleName -eq "Heading 1" -or $styleName -eq "Heading 2") {
            $style.Font.Size = 14
            $style.Font.Bold = 1
            $style.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
            $style.ParagraphFormat.SpaceAfter = 6
        } else {
            $style.Font.Size = 12
            $style.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
            $style.ParagraphFormat.SpaceAfter = 6
        }
    }

    if ($doc.Tables.Count -ge 1) {
        $guideCell = $doc.Tables.Item(1).Cell(1, 1).Range
        $guideCell.Text = "Guided by:`rDr.Mamta.P.Khanchandani"
        $guideCell.Font.Name = "Times New Roman"
        $guideCell.Font.Size = 12
    }

    $centerTitles = @(
        "COLLEGE CERTIFICATE",
        "ACKNOWLEDGEMENT",
        "INDEX"
    )

    $mainHeadings = @(
        "1. Introduction to Project",
        "2. Technology Used",
        "3. Objectives",
        "4. System Flow Chart / Site Diagram",
        "5. Database Design",
        "6. Screenshots (Input, Output)",
        "7. References"
    )

    $subHeadings = @(
        "2.1 Frontend (Android)",
        "2.2 Backend (Firebase + Supabase)",
        "2.3 Development Environment",
        "4.1 Application Navigation Flow",
        "4.2 How a Booking Works",
        "4.3 Screen Navigation Structure",
        "6.1 Input Screens",
        "6.2 Output Screens"
    )

    $indexParagraph = $null
    $guideUpdated = $false

    foreach ($para in $doc.Paragraphs) {
        $text = Clean-Text $para.Range.Text
        if (-not $text) {
            continue
        }

        $para.Range.Font.Name = "Times New Roman"

        if ($text -eq "Dr.Mamta Khanchandani" -or $text -eq "Dr.Mamta.P.Khanchandani") {
            $para.Range.Text = "Dr.Mamta.P.Khanchandani"
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.Font.Size = 12
            $guideUpdated = $true
            continue
        }

        if ($centerTitles -contains $text) {
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.Font.Size = 14
            $para.Range.Bold = 1
            $para.Range.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
            $para.Range.ParagraphFormat.SpaceAfter = 6
            $para.Alignment = $wdAlignParagraphCenter
            if ($text -eq "INDEX") {
                $indexParagraph = $para
            }
            continue
        }

        if ($mainHeadings -contains $text) {
            $para.Range.Style = $doc.Styles.Item("Heading 1")
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.Font.Size = 14
            $para.Range.Bold = 1
            $para.Alignment = $wdAlignParagraphLeft
            continue
        }

        if ($subHeadings -contains $text) {
            $para.Range.Style = $doc.Styles.Item("Heading 2")
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.Font.Size = 14
            $para.Range.Bold = 1
            $para.Alignment = $wdAlignParagraphLeft
            continue
        }

        if ($text -like "Figure *") {
            $para.Range.Font.Size = 12
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
            $para.Alignment = $wdAlignParagraphCenter
            continue
        }

        if ($text -like "[[]*Screenshot will be placed here[]]") {
            $para.Range.Font.Size = 12
            $para.Range.Font.Name = "Times New Roman"
            $para.Alignment = $wdAlignParagraphCenter
            continue
        }

        if ($text -match '^[A-Z][A-Za-z ]+\(.*\)$' -or $text -match '^(Login Screen|Registration Screen|Add Room Dialog \(Admin\)|Edit Room Dialog \(Admin\)|Workspace Settings Dialog \(Admin\)|Filter Dialog|Booking Screen|Home Screen \(User\)|Room Details Screen|Profile Screen with Bookings|Admin Dashboard|Room Bookings Dialog \(Admin\)|Collection 1: users|Collection 2: rooms|Collection 3: bookings|Collection 4: workspaces|Collection Relationships|Supabase Storage Structure|User Side Navigation:|Admin Side Navigation:)$') {
            $para.Range.Font.Size = 12
            $para.Range.Font.Name = "Times New Roman"
            $para.Range.Bold = 1
            $para.Range.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
            $para.Alignment = $wdAlignParagraphLeft
            continue
        }

        $para.Range.Font.Size = 12
        $para.Range.Font.Name = "Times New Roman"
        $para.Range.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
        $para.Range.ParagraphFormat.SpaceAfter = 6
        $para.Alignment = $wdAlignParagraphJustify
    }

    if (-not $guideUpdated) {
        $find = $doc.Content.Find
        $find.ClearFormatting()
        $find.Text = "Dr.Mamta Khanchandani"
        $find.Replacement.ClearFormatting()
        $find.Replacement.Text = "Dr.Mamta.P.Khanchandani"
        $find.Execute($find.Text, $false, $false, $false, $false, $false, $true, 1, $false, $find.Replacement.Text, 2) | Out-Null
    }

    foreach ($table in $doc.Tables) {
        $table.Range.Font.Name = "Times New Roman"
        $table.Range.Font.Size = 12
        $table.Range.ParagraphFormat.LineSpacingRule = $wdLineSpace1pt5
        foreach ($row in $table.Rows) {
            $row.HeightRule = $wdRowHeightAtLeast
            $row.Height = 22
        }
        foreach ($cell in $table.Range.Cells) {
            $cell.TopPadding = 6
            $cell.BottomPadding = 6
            $cell.LeftPadding = 6
            $cell.RightPadding = 6
        }
    }

    if ($doc.Tables.Count -ge 2) {
        $doc.Tables.Item(2).Delete()
    }

    if ($indexParagraph -ne $null) {
        $tocRange = $indexParagraph.Range.Duplicate
        $tocRange.Collapse(0)
        $tocRange.InsertParagraphAfter() | Out-Null
        $tocRange.Collapse(0)
        $doc.TablesOfContents.Add($tocRange, $true, 1, 2, $true, "", $true, $true, $true, $true) | Out-Null
    }

    foreach ($section in $doc.Sections) {
        $section.PageSetup.DifferentFirstPageHeaderFooter = $true

        $headerRange = $section.Headers.Item($wdHeaderFooterPrimary).Range
        $headerRange.Text = "DESKIFY WORKSPACE BOOKING APP"
        $headerRange.Font.Name = "Times New Roman"
        $headerRange.Font.Size = 12
        $headerRange.Font.Bold = 1
        $headerRange.ParagraphFormat.Alignment = $wdAlignParagraphCenter

        $section.Headers.Item($wdHeaderFooterFirstPage).Range.Text = ""

        $footerPrimary = $section.Footers.Item($wdHeaderFooterPrimary)
        $footerPrimary.Range.Text = ""
        $footerPrimary.Range.ParagraphFormat.Alignment = $wdAlignParagraphCenter
        $footerPrimary.PageNumbers.RestartNumberingAtSection = $false
        $footerPrimary.PageNumbers.Add() | Out-Null
        $footerPrimary.Range.Font.Name = "Times New Roman"
        $footerPrimary.Range.Font.Size = 12

        $section.Footers.Item($wdHeaderFooterFirstPage).Range.Text = ""
    }

    $doc.Repaginate()
    foreach ($toc in $doc.TablesOfContents) {
        $toc.Update()
    }
    $doc.Fields.Update() | Out-Null
    $doc.Repaginate()
    $null = $doc.ComputeStatistics($wdStatisticPages)
    foreach ($toc in $doc.TablesOfContents) {
        $toc.Update()
    }

    $doc.Save()
    $doc.Close()
}
finally {
    $word.Quit()
}
