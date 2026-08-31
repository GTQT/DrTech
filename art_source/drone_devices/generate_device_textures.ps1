param(
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '..\..\src\main\resources\assets\drtech\textures\blocks\overlay\drone'),
    [string]$CasingOutputDirectory = (Join-Path $PSScriptRoot '..\..\src\main\resources\assets\drtech\textures\blocks\casings\drone')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
New-Item -ItemType Directory -Force -Path $CasingOutputDirectory | Out-Null

function New-DeviceCasing([string]$Name, [int[]]$Accent, [string]$Glyph) {
    $deviceDirectory = Join-Path $CasingOutputDirectory $Name
    New-Item -ItemType Directory -Force -Path $deviceDirectory | Out-Null
    foreach ($face in @('top', 'bottom', 'side')) {
        $bitmap = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $g = [System.Drawing.Graphics]::FromImage($bitmap)
            try {
                $g.Clear([System.Drawing.Color]::FromArgb(255, 24, 30, 36))
                $dark = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 13, 17, 22))
                $steel = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 55, 65, 74))
                $mid = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 75, 87, 96))
                $accentDarkBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,
                    [Math]::Max(0, $Accent[0] - 95), [Math]::Max(0, $Accent[1] - 95), [Math]::Max(0, $Accent[2] - 95)))
                $accentBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, $Accent[0], $Accent[1], $Accent[2]))
                $accentBrightBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,
                    [Math]::Min(255, $Accent[0] + 40), [Math]::Min(255, $Accent[1] + 40), [Math]::Min(255, $Accent[2] + 40)))
                try {
                    $g.FillRectangle($dark, 0, 0, 16, 1); $g.FillRectangle($dark, 0, 15, 16, 1)
                    $g.FillRectangle($dark, 0, 0, 1, 16); $g.FillRectangle($dark, 15, 0, 1, 16)
                    $g.FillRectangle($steel, 1, 1, 14, 2); $g.FillRectangle($steel, 1, 13, 14, 2)
                    $g.FillRectangle($mid, 2, 3, 12, 1); $g.FillRectangle($mid, 2, 12, 12, 1)
                    $g.FillRectangle($accentDarkBrush, 2, 5, 12, 6)
                    if ($face -eq 'side') {
                        $g.FillRectangle($accentBrush, 2, 7, 12, 2)
                        $g.FillRectangle($accentBrightBrush, 4, 6, 2, 1)
                        $g.FillRectangle($accentBrightBrush, 10, 9, 2, 1)
                        $g.FillRectangle($steel, 3, 4, 1, 2); $g.FillRectangle($steel, 12, 10, 1, 2)
                    } elseif ($face -eq 'bottom') {
                        $g.FillRectangle($steel, 3, 4, 10, 8)
                        $g.FillRectangle($dark, 5, 6, 6, 4)
                        $g.FillRectangle($accentBrush, 7, 7, 2, 2)
                        $g.FillRectangle($dark, 2, 2, 2, 2); $g.FillRectangle($dark, 12, 2, 2, 2)
                        $g.FillRectangle($dark, 2, 12, 2, 2); $g.FillRectangle($dark, 12, 12, 2, 2)
                    } else {
                        $g.FillRectangle($steel, 3, 4, 10, 8)
                        $g.FillRectangle($accentBrush, 4, 5, 8, 6)
                        $g.FillRectangle($accentBrightBrush, 5, 5, 6, 1)
                        $g.FillRectangle($accentDarkBrush, 5, 10, 6, 1)
                        switch ($Glyph) {
                            'dock' { $g.FillRectangle($dark, 7, 6, 2, 4); $g.FillRectangle($dark, 6, 7, 4, 2) }
                            'programmer' { $g.FillRectangle($dark, 5, 6, 6, 4); $g.FillRectangle($accentBrightBrush, 6, 7, 2, 1); $g.FillRectangle($accentBrightBrush, 9, 9, 1, 1) }
                            'controller' { $g.FillRectangle($dark, 5, 8, 2, 2); $g.FillRectangle($dark, 9, 8, 2, 2); $g.FillRectangle($dark, 7, 6, 2, 2) }
                            'item' { $g.FillRectangle($dark, 5, 7, 6, 3); $g.FillRectangle($accentBrightBrush, 6, 6, 4, 1) }
                            'fluid' { $g.FillRectangle($dark, 7, 6, 2, 1); $g.FillRectangle($dark, 6, 7, 4, 3); $g.FillRectangle($accentBrightBrush, 7, 9, 2, 1) }
                            'eu' { $g.FillRectangle($dark, 8, 6, 2, 2); $g.FillRectangle($dark, 6, 8, 3, 2); $g.FillRectangle($dark, 7, 10, 2, 1) }
                        }
                    }
                } finally {
                    $dark.Dispose(); $steel.Dispose(); $mid.Dispose(); $accentDarkBrush.Dispose(); $accentBrush.Dispose(); $accentBrightBrush.Dispose()
                }
            } finally { $g.Dispose() }
            $bitmap.Save((Join-Path $deviceDirectory ($face + '.png')), [System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $bitmap.Dispose() }
    }
}

function New-DeviceOverlay([string]$Name, [int[]]$Accent, [string]$Glyph) {
    $bitmap = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $g = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $g.Clear([System.Drawing.Color]::Transparent)
            $edge = [System.Drawing.Color]::FromArgb(235, $Accent[0], $Accent[1], $Accent[2])
            $dim = [System.Drawing.Color]::FromArgb(190, [Math]::Max(0,$Accent[0]-70), [Math]::Max(0,$Accent[1]-70), [Math]::Max(0,$Accent[2]-70))
            $bright = [System.Drawing.Color]::FromArgb(255, [Math]::Min(255,$Accent[0]+45), [Math]::Min(255,$Accent[1]+45), [Math]::Min(255,$Accent[2]+45))
            $edgePen = [System.Drawing.Pen]::new($edge, 1)
            $dimPen = [System.Drawing.Pen]::new($dim, 1)
            $brightPen = [System.Drawing.Pen]::new($bright, 1)
            try {
                $g.DrawRectangle($dimPen, 1, 1, 13, 13)
                $g.DrawLine($edgePen, 3, 3, 12, 3)
                $g.DrawLine($edgePen, 3, 12, 12, 12)
                switch ($Glyph) {
                    'dock' {
                        $g.DrawEllipse($brightPen, 4, 4, 7, 7); $g.DrawEllipse($edgePen, 6, 6, 3, 3)
                        $g.DrawLine($edgePen, 7, 1, 7, 4); $g.DrawLine($edgePen, 7, 11, 7, 14)
                    }
                    'programmer' {
                        $g.DrawRectangle($edgePen, 4, 4, 7, 6); $g.DrawLine($brightPen, 5, 6, 7, 8)
                        $g.DrawLine($brightPen, 7, 8, 5, 9); $g.DrawLine($edgePen, 8, 9, 10, 9)
                    }
                    'controller' {
                        $g.DrawLine($edgePen, 4, 10, 7, 7); $g.DrawLine($edgePen, 7, 7, 11, 10)
                        $g.FillEllipse([System.Drawing.SolidBrush]::new($bright), 3, 9, 3, 3)
                        $g.FillEllipse([System.Drawing.SolidBrush]::new($bright), 6, 5, 3, 3)
                        $g.FillEllipse([System.Drawing.SolidBrush]::new($bright), 10, 9, 3, 3)
                    }
                    'item' {
                        $g.DrawRectangle($edgePen, 4, 5, 7, 6); $g.DrawLine($brightPen, 4, 7, 11, 7)
                        $g.DrawLine($brightPen, 6, 4, 9, 4)
                    }
                    'fluid' {
                        $points = [System.Drawing.Point[]]@([System.Drawing.Point]::new(8,4),[System.Drawing.Point]::new(4,9),[System.Drawing.Point]::new(5,12),[System.Drawing.Point]::new(11,12),[System.Drawing.Point]::new(12,9))
                        $g.DrawPolygon($edgePen, $points); $g.DrawLine($brightPen, 6, 10, 10, 10)
                    }
                    'eu' {
                        $points = [System.Drawing.Point[]]@([System.Drawing.Point]::new(9,3),[System.Drawing.Point]::new(5,8),[System.Drawing.Point]::new(8,8),[System.Drawing.Point]::new(6,13),[System.Drawing.Point]::new(12,7),[System.Drawing.Point]::new(9,7))
                        $g.DrawLines($brightPen, $points)
                    }
                }
            } finally { $edgePen.Dispose(); $dimPen.Dispose(); $brightPen.Dispose() }
        } finally { $g.Dispose() }
        $bitmap.Save((Join-Path $OutputDirectory ($Name + '.png')), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $bitmap.Dispose() }
}

New-DeviceOverlay 'dock'       @(36, 194, 244) 'dock'
New-DeviceOverlay 'programmer' @(74, 214, 242) 'programmer'
New-DeviceOverlay 'controller' @(174, 88, 246) 'controller'
New-DeviceOverlay 'endpoint_item'  @(244, 142, 42) 'item'
New-DeviceOverlay 'endpoint_fluid' @(42, 166, 246) 'fluid'
New-DeviceOverlay 'endpoint_eu'    @(250, 218, 48) 'eu'

New-DeviceCasing 'dock'           @(36, 194, 244) 'dock'
New-DeviceCasing 'programmer'     @(74, 214, 242) 'programmer'
New-DeviceCasing 'controller'     @(174, 88, 246) 'controller'
New-DeviceCasing 'endpoint_item'  @(244, 142, 42) 'item'
New-DeviceCasing 'endpoint_fluid' @(42, 166, 246) 'fluid'
New-DeviceCasing 'endpoint_eu'    @(250, 218, 48) 'eu'
