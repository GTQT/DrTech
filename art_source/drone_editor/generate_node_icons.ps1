param(
    [string]$RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
)

Add-Type -AssemblyName System.Drawing

$languageFile = Join-Path $RepositoryRoot 'src\main\resources\assets\drtech\lang\en_us.lang'
$outputDirectory = Join-Path $RepositoryRoot 'src\main\resources\assets\drtech\textures\gui\drone\nodes'
[System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null

$paths = [System.Collections.Generic.List[string]]::new()
foreach ($line in [System.IO.File]::ReadAllLines($languageFile, [System.Text.Encoding]::UTF8)) {
    if ($line -match '^drtech\.drone\.node\.([a-z0-9_]+)=') {
        $path = $Matches[1]
        if (-not $paths.Contains($path)) { $paths.Add($path) }
    }
}

function Get-NodePalette([string]$path) {
    if ($path -match 'entity|attack|combat|follow|flee|interact') { return @(220, 73, 82) }
    if ($path -match 'fluid|water|fish') { return @(54, 151, 222) }
    if ($path -match 'energy|eu|charge|machine') { return @(246, 190, 52) }
    if ($path -match 'area|coordinate|position|path|move|dock') { return @(75, 202, 173) }
    if ($path -match 'item|inventory|craft|filter|pickup|drop') { return @(177, 116, 224) }
    if ($path -match 'string|number|boolean|variable|compare|math') { return @(93, 175, 236) }
    if ($path -match 'break|place|block|crop|harvest') { return @(119, 184, 89) }
    return @(149, 164, 184)
}

foreach ($path in $paths) {
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try { $hash = $sha.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($path)) }
    finally { $sha.Dispose() }
    $palette = Get-NodePalette $path
    $bitmap = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.Clear([System.Drawing.Color]::Transparent)
        $border = [System.Drawing.Color]::FromArgb(255, $palette[0], $palette[1], $palette[2])
        $dark = [System.Drawing.Color]::FromArgb(240, 18, 25, 34)
        $bright = [System.Drawing.Color]::FromArgb(255,
            [Math]::Min(255, $palette[0] + 45),
            [Math]::Min(255, $palette[1] + 45),
            [Math]::Min(255, $palette[2] + 45))
        $graphics.FillRectangle([System.Drawing.SolidBrush]::new($dark), 1, 1, 14, 14)
        $pen = [System.Drawing.Pen]::new($border, 1)
        try { $graphics.DrawRectangle($pen, 1, 1, 13, 13) } finally { $pen.Dispose() }
        $brush = [System.Drawing.SolidBrush]::new($bright)
        try {
            for ($index = 0; $index -lt 6; $index++) {
                $x = 3 + ($hash[$index] % 10)
                $y = 3 + ($hash[$index + 6] % 10)
                $size = 1 + ($hash[$index + 12] % 3)
                $graphics.FillRectangle($brush, $x, $y, [Math]::Min($size, 14 - $x), [Math]::Min($size, 14 - $y))
            }
            $graphics.DrawLine([System.Drawing.Pen]::new($bright, 1),
                3 + ($hash[20] % 4), 3 + ($hash[21] % 10),
                9 + ($hash[22] % 4), 3 + ($hash[23] % 10))
        } finally { $brush.Dispose() }
        $target = Join-Path $outputDirectory ($path + '.png')
        $bitmap.Save($target, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $graphics.Dispose()
        $bitmap.Dispose()
    }
}

# Extension nodes without a bundled icon use this explicit fallback.
$missing = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$missingGraphics = [System.Drawing.Graphics]::FromImage($missing)
try {
    $missingGraphics.Clear([System.Drawing.Color]::Transparent)
    $missingGraphics.FillRectangle([System.Drawing.Brushes]::DarkSlateGray, 1, 1, 14, 14)
    $missingGraphics.DrawRectangle([System.Drawing.Pens]::Magenta, 1, 1, 13, 13)
    $missingGraphics.DrawLine([System.Drawing.Pens]::Magenta, 4, 4, 11, 11)
    $missingGraphics.DrawLine([System.Drawing.Pens]::Magenta, 11, 4, 4, 11)
    $missing.Save((Join-Path $outputDirectory 'missing.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $missingGraphics.Dispose()
    $missing.Dispose()
}

Write-Output ("Generated {0} node icons in {1}" -f $paths.Count, $outputDirectory)
