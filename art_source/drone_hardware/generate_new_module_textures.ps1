param(
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '..\..\src\main\resources\assets\drtech\textures\items\drone')
)

Add-Type -AssemblyName System.Drawing
$ErrorActionPreference = 'Stop'
$output = [IO.Path]::GetFullPath($OutputDirectory)
[IO.Directory]::CreateDirectory($output) | Out-Null

function Brush([string]$hex) { New-Object Drawing.SolidBrush ([Drawing.ColorTranslator]::FromHtml($hex)) }
function Pen([string]$hex, [float]$width = 1) { New-Object Drawing.Pen ([Drawing.ColorTranslator]::FromHtml($hex)), $width }
function Rect($g, [int]$x, [int]$y, [int]$w, [int]$h, [string]$color) {
    $b = Brush $color; try { $g.FillRectangle($b, $x, $y, $w, $h) } finally { $b.Dispose() }
}
function Line($g, [int]$x1, [int]$y1, [int]$x2, [int]$y2, [string]$color, [float]$width = 1) {
    $p = Pen $color $width; try { $g.DrawLine($p, $x1, $y1, $x2, $y2) } finally { $p.Dispose() }
}
function Poly($g, [object[]]$coords, [string]$color) {
    $points = New-Object 'Drawing.Point[]' ($coords.Count)
    for ($i = 0; $i -lt $coords.Count; $i++) { $points[$i] = New-Object Drawing.Point $coords[$i][0], $coords[$i][1] }
    $b = Brush $color; try { $g.FillPolygon($b, $points) } finally { $b.Dispose() }
}
function Base($g, [string]$accent) {
    Poly $g @(@(6,1),@(25,1),@(30,6),@(30,25),@(25,30),@(6,30),@(1,25),@(1,6)) '#11171c'
    Poly $g @(@(7,2),@(24,2),@(28,6),@(28,25),@(24,28),@(7,28),@(3,24),@(3,7)) '#68737b'
    Poly $g @(@(8,4),@(23,4),@(27,8),@(27,23),@(23,27),@(8,27),@(5,23),@(5,8)) '#171d22'
    Rect $g 9 3 14 2 '#9aa3a8'; Rect $g 10 4 12 1 $accent
    Rect $g 9 27 14 2 '#3e474d'; Rect $g 11 27 10 1 $accent
    Rect $g 3 10 2 12 '#2d353b'; Rect $g 3 12 1 8 $accent
    Rect $g 27 10 2 12 '#2d353b'; Rect $g 28 12 1 8 $accent
    Rect $g 6 6 2 2 '#aab1b5'; Rect $g 24 6 2 2 '#aab1b5'
    Rect $g 6 24 2 2 '#353d43'; Rect $g 24 24 2 2 '#353d43'
}
function GlowPixel($g, [int]$x, [int]$y, [string]$dark, [string]$bright) {
    Rect $g ($x-1) ($y-1) 3 3 $dark; Rect $g $x $y 1 1 $bright
}

$modules = @(
    @{ Name='advanced_navigation'; Accent='#31e6ff'; Draw={ param($g)
        Poly $g @(@(16,7),@(19,14),@(25,16),@(19,18),@(16,25),@(13,18),@(7,16),@(13,14)) '#1b6172'
        Poly $g @(@(16,8),@(18,15),@(24,16),@(18,17),@(16,24),@(14,17),@(8,16),@(14,15)) '#38e9ff'
        Rect $g 14 14 5 5 '#dffcff'; Rect $g 15 15 3 3 '#17677a'; Rect $g 16 9 1 5 '#ffffff'
    }}
    @{ Name='eu_interface'; Accent='#ffe45c'; Draw={ param($g)
        Rect $g 9 10 14 13 '#5b4a13'; Rect $g 10 11 12 11 '#f2c62c'; Rect $g 12 8 3 5 '#d8e2e6'; Rect $g 18 8 3 5 '#d8e2e6'
        Poly $g @(@(17,11),@(13,17),@(16,17),@(14,23),@(21,15),@(18,15)) '#fff8b0'; Rect $g 11 20 10 2 '#815b0a'
    }}
    @{ Name='tool_arm'; Accent='#ff9b2f'; Draw={ param($g)
        Line $g 10 22 19 13 '#5d6870' 5; Line $g 10 22 19 13 '#cbd2d5' 2
        Poly $g @(@(17,8),@(21,7),@(24,10),@(22,12),@(20,10),@(18,12),@(20,14),@(18,17),@(14,13),@(17,10)) '#ff9b2f'
        Rect $g 7 20 7 5 '#515b62'; Rect $g 8 21 5 3 '#d5dadd'; GlowPixel $g 9 22 '#9b4f13' '#ffd39a'
    }}
    @{ Name='entity_scanner'; Accent='#54f0a8'; Draw={ param($g)
        Poly $g @(@(6,16),@(10,11),@(16,9),@(22,11),@(26,16),@(22,21),@(16,23),@(10,21)) '#19543e'
        Poly $g @(@(8,16),@(11,13),@(16,11),@(21,13),@(24,16),@(21,19),@(16,21),@(11,19)) '#55e9a7'
        Rect $g 13 13 7 7 '#092d27'; Rect $g 15 15 3 3 '#d9fff1'; GlowPixel $g 16 16 '#16a972' '#ffffff'
    }}
    @{ Name='combat'; Accent='#ff4b43'; Draw={ param($g)
        Line $g 9 23 23 9 '#751b1b' 5; Line $g 9 23 23 9 '#e4e7e8' 2
        Line $g 9 9 23 23 '#751b1b' 5; Line $g 9 9 23 23 '#e4e7e8' 2
        Rect $g 7 21 6 3 '#ff4b43'; Rect $g 20 21 5 3 '#ff4b43'; Rect $g 14 14 5 5 '#501014'; Rect $g 15 15 3 3 '#ff6a55'
    }}
    @{ Name='entity_containment'; Accent='#43a8ff'; Draw={ param($g)
        Poly $g @(@(11,7),@(21,7),@(24,11),@(24,21),@(21,25),@(11,25),@(8,21),@(8,11)) '#173b5e'
        Rect $g 10 10 12 12 '#318ed8'; Rect $g 12 12 8 8 '#0c253e'; Rect $g 14 13 4 6 '#7fd6ff'
        Rect $g 13 9 6 2 '#d8e6ed'; Rect $g 13 21 6 2 '#67757d'; GlowPixel $g 16 16 '#226ea4' '#e8fbff'
    }}
    @{ Name='waterproof'; Accent='#3de5ff'; Draw={ param($g)
        Poly $g @(@(16,7),@(23,17),@(22,22),@(19,25),@(13,25),@(10,22),@(9,17)) '#145b78'
        Poly $g @(@(16,9),@(21,17),@(20,21),@(18,23),@(14,23),@(11,20),@(11,17)) '#35cfee'
        Rect $g 13 17 2 4 '#a9f6ff'; Rect $g 14 15 2 2 '#dfffff'; Line $g 8 25 24 25 '#5baec1' 1
    }}
    @{ Name='self_repair'; Accent='#67ff63'; Draw={ param($g)
        Rect $g 13 7 6 18 '#24652c'; Rect $g 7 13 18 6 '#24652c'; Rect $g 14 8 4 16 '#65ed62'; Rect $g 8 14 16 4 '#65ed62'
        Rect $g 14 14 4 4 '#e6ffe4'; Rect $g 7 7 3 3 '#7d8b91'; Rect $g 22 22 3 3 '#7d8b91'
        GlowPixel $g 8 8 '#24652c' '#baffb6'; GlowPixel $g 23 23 '#24652c' '#baffb6'
    }}
    @{ Name='secure_access'; Accent='#ffd04d'; Draw={ param($g)
        Poly $g @(@(16,7),@(24,10),@(23,19),@(20,24),@(16,26),@(12,24),@(9,19),@(8,10)) '#745612'
        Poly $g @(@(16,9),@(22,11),@(21,18),@(19,22),@(16,24),@(13,22),@(11,18),@(10,11)) '#f1bd32'
        Rect $g 13 14 7 7 '#22282d'; Rect $g 14 12 5 5 '#dce2e5'; Rect $g 15 13 3 4 '#22282d'; Rect $g 15 16 3 3 '#fff3a0'
    }}
    @{ Name='advanced_item_handling'; Accent='#ff9d35'; Draw={ param($g)
        Rect $g 11 11 10 10 '#7b3f0e'; Rect $g 12 12 8 8 '#ed841d'; Line $g 12 15 20 15 '#ffc36b' 1; Line $g 16 12 16 20 '#a74b0b' 1
        Poly $g @(@(7,13),@(11,9),@(11,12),@(15,12),@(15,14),@(11,14),@(11,17)) '#5cf0ff'
        Poly $g @(@(25,19),@(21,23),@(21,20),@(17,20),@(17,18),@(21,18),@(21,15)) '#5cf0ff'
    }}
    @{ Name='fleet_communication'; Accent='#b56cff'; Draw={ param($g)
        Line $g 10 21 16 11 '#77509a' 2; Line $g 22 21 16 11 '#77509a' 2; Line $g 10 21 22 21 '#77509a' 2
        Rect $g 13 8 7 7 '#61358b'; Rect $g 15 10 3 3 '#e5c5ff'; Rect $g 7 18 7 7 '#274c71'; Rect $g 9 20 3 3 '#77d9ff'; Rect $g 19 18 7 7 '#274c71'; Rect $g 21 20 3 3 '#77d9ff'
        GlowPixel $g 16 11 '#6c3c93' '#ffffff'
    }}
    @{ Name='fishing'; Accent='#45bfff'; Draw={ param($g)
        Line $g 10 9 20 9 '#bdc8cd' 2; Line $g 20 9 20 19 '#bdc8cd' 2; Line $g 20 19 17 22 '#bdc8cd' 2
        Rect $g 8 8 4 4 '#8e581c'; Rect $g 9 9 2 2 '#ffb84c'; Rect $g 16 21 4 3 '#3f95c3'
        Poly $g @(@(8,18),@(13,14),@(19,16),@(15,21),@(10,22)) '#2d8fc1'; Poly $g @(@(8,18),@(5,15),@(6,21)) '#54d4ff'; Rect $g 12 17 2 2 '#d9f9ff'
    }}
    @{ Name='thaumcraft_alchemy'; Accent='#a45cff'; Draw={ param($g)
        Rect $g 10 8 12 4 '#6d7480'; Rect $g 9 11 14 13 '#352044'; Rect $g 11 12 10 10 '#7a35a8'
        Rect $g 12 15 8 6 '#bd65ef'; Rect $g 14 16 4 4 '#f0c8ff'; Rect $g 12 23 8 2 '#777f88'
        Line $g 7 8 11 12 '#e3d8ea' 2; Line $g 25 8 21 12 '#e3d8ea' 2; GlowPixel $g 16 18 '#7d2cae' '#ffffff'
    }}
)

foreach ($module in $modules) {
    $bitmap = New-Object Drawing.Bitmap 32, 32, ([Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $g = [Drawing.Graphics]::FromImage($bitmap)
        try {
            $g.Clear([Drawing.Color]::Transparent)
            $g.SmoothingMode = [Drawing.Drawing2D.SmoothingMode]::None
            $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
            Base $g $module.Accent
            & $module.Draw $g
        } finally { $g.Dispose() }
        $path = Join-Path $output ("drone_upgrade_{0}.png" -f $module.Name)
        $bitmap.Save($path, [Drawing.Imaging.ImageFormat]::Png)
    } finally { $bitmap.Dispose() }
}

$atlas = New-Object Drawing.Bitmap 128, 128, ([Drawing.Imaging.PixelFormat]::Format32bppArgb)
try {
    $atlasGraphics = [Drawing.Graphics]::FromImage($atlas)
    try {
        $atlasGraphics.Clear([Drawing.Color]::Transparent)
        $atlasGraphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        for ($index = 0; $index -lt $modules.Count; $index++) {
            $iconPath = Join-Path $output ("drone_upgrade_{0}.png" -f $modules[$index].Name)
            $icon = [Drawing.Image]::FromFile($iconPath)
            try { $atlasGraphics.DrawImageUnscaled($icon, ($index % 4) * 32, [Math]::Floor($index / 4) * 32) }
            finally { $icon.Dispose() }
        }
    } finally { $atlasGraphics.Dispose() }
    $atlas.Save((Join-Path $PSScriptRoot 'drone_upgrade_modules_7_18_atlas.png'),
            [Drawing.Imaging.ImageFormat]::Png)
} finally { $atlas.Dispose() }

Write-Output ("Generated {0} module textures in {1}" -f $modules.Count, $output)
