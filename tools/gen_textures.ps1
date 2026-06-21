# Generates custom 16x16 pixel-art item textures for the modded governance + cursed gear.
# Run from the repo root:  powershell -ExecutionPolicy Bypass -File tools\gen_textures.ps1
Add-Type -AssemblyName System.Drawing

$outDir = "src\main\resources\assets\politicalserver\textures\item"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# Palette: char -> [a,r,g,b]  (case-sensitive dictionary so 'Y' != 'y')
$pal = New-Object 'System.Collections.Generic.Dictionary[string,int[]]'
$pal['.'] = @(0,0,0,0)
$pal['o'] = @(255,22,18,30)      # outline
$pal['k'] = @(255,12,12,16)
$pal['g'] = @(255,170,170,185)   # steel
$pal['G'] = @(255,215,215,230)   # steel light
$pal['s'] = @(255,232,232,242)   # silver highlight
$pal['b'] = @(255,120,72,32)     # brown
$pal['B'] = @(255,165,110,60)    # brown light
$pal['y'] = @(255,255,205,50)    # gold
$pal['Y'] = @(255,255,235,130)   # gold light
$pal['p'] = @(255,150,50,205)    # cursed purple
$pal['P'] = @(255,205,140,255)   # cursed purple light
$pal['d'] = @(255,70,22,95)      # dark purple
$pal['w'] = @(255,238,234,214)   # paper
$pal['W'] = @(255,205,200,176)   # paper shadow
$pal['r'] = @(255,205,45,45)     # red
$pal['e'] = @(255,45,175,85)     # green
$pal['E'] = @(255,90,210,120)    # green light
$pal['i'] = @(255,45,70,170)     # blue
$pal['I'] = @(255,80,120,210)    # blue light
$pal['m'] = @(255,205,55,120)    # magenta seal
$pal['c'] = @(255,70,200,200)    # cyan

function Save-Icon($name, $rows) {
  $bmp = New-Object System.Drawing.Bitmap 16,16
  for ($y=0; $y -lt 16; $y++) {
    $line = $rows[$y]
    for ($x=0; $x -lt 16; $x++) {
      $c = [string]$line[$x]
      if ($pal.ContainsKey($c)) { $rgba = $pal[$c] } else { $rgba = @(0,0,0,0) }
      $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($rgba[0],$rgba[1],$rgba[2],$rgba[3]))
    }
  }
  $path = Join-Path $outDir "$name.png"
  $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
  $bmp.Dispose()
  Write-Host "wrote $path"
}

$crown = @(
  "................",
  "................",
  "................",
  "................",
  "...o.o.o.o.o....",
  "...oyoyoyoyo....",
  "...oyYyYyYyo....",
  "...oyYrYYrYyo...",
  "...oyyyyyyyyo...",
  "...oyyyyyyyyo...",
  "...oooooooooo...",
  "................",
  "................",
  "................",
  "................",
  "................"
)

$gavel = @(
  "................",
  "...oooooooo.....",
  "...obbbbbbo.....",
  "...obBBBBbo.....",
  "...obbbbbbo.....",
  "....oobboo......",
  ".....obbo.......",
  ".....obBo.......",
  ".....obbo.......",
  ".....obbo.......",
  "....obbbbo......",
  "...obbbbbbo.....",
  "...obBBBBbo.....",
  "...obbbbbbo.....",
  "...oooooooo.....",
  "................"
)

# vertical sword, purple blade
$cursed_blade = @(
  "................",
  ".......oo.......",
  "......oPpo......",
  "......oppo......",
  "......oppo......",
  "......oppo......",
  "......oppo......",
  "......oppo......",
  "....ooppppoo....",
  "......obbo......",
  "......obBo......",
  "......obbo......",
  "......oddo......",
  "................",
  "................",
  "................"
)

$cursed_dagger = @(
  "................",
  "................",
  "................",
  ".......oo.......",
  "......oPo.......",
  "......opo.......",
  "......opo.......",
  "......opo.......",
  ".....oopoo......",
  "......obo.......",
  "......obo.......",
  "......odo.......",
  "................",
  "................",
  "................",
  "................"
)

$cursed_greatsword = @(
  "................",
  "......oooo......",
  ".....oPppo......",
  ".....opppo......",
  ".....opppo......",
  ".....opppo......",
  ".....opppo......",
  ".....opppo......",
  ".....opppo......",
  "...ooppppoo.....",
  "....oobboo......",
  ".....obbo.......",
  ".....obBo.......",
  ".....oddo.......",
  "................",
  "................"
)

$cursed_polearm = @(
  "................",
  "............oo..",
  "...........oPpo.",
  "..........oppo..",
  ".........oppo...",
  "........oodo....",
  ".......obo......",
  "......obo.......",
  ".....obo........",
  "....obo.........",
  "...obo..........",
  "..obo...........",
  "..oo............",
  "................",
  "................",
  "................"
)

$cursed_whip = @(
  "................",
  "...oo...........",
  "..obbo..........",
  "..obBo..........",
  "...opo..........",
  "...opoo.........",
  "....oppo........",
  ".....oppo.......",
  "......oppo......",
  ".......oppo.....",
  "........oppo....",
  ".........opo....",
  "..........opo...",
  "...........opo..",
  "............oP..",
  "................"
)

$soul_cleaver = @(
  "................",
  ".....ooooo......",
  "....odpppdo.....",
  "...odpPPPpdo....",
  "...odpppppdo....",
  "...odppppdo.....",
  "....oddddo.bo...",
  ".......obbo.....",
  "......obBo......",
  "......obbo......",
  ".....obbo.......",
  ".....obo........",
  "....odo.........",
  "....oo..........",
  "................",
  "................"
)

$ballot = @(
  "................",
  "..oooooooooo....",
  "..owwwwwwwwo....",
  "..owWWWWWWwo....",
  "..owwwwwwewo....",
  "..owwwwweewo....",
  "..oweewweewo....",
  "..oweeeeewwo....",
  "..owweeewwwo....",
  "..owwweewwwo....",
  "..owwwwwwwwo....",
  "..owWWWWWWwo....",
  "..owwwwwwwwo....",
  "..oooooooooo....",
  "................",
  "................"
)

$decree_scroll = @(
  "................",
  "................",
  "..bbbbbbbbbbbb..",
  ".obwwwwwwwwwwbo.",
  ".obwWWWWWWWWwbo.",
  ".obwwwwwwwwwwbo.",
  ".obwwoooooowwbo.",
  ".obwwwwwwwwwwbo.",
  ".obwwoooooowwbo.",
  ".obwwwwwwwwwwbo.",
  ".obwwoooowwwwbo.",
  ".obwWWWWWWWWwbo.",
  ".obwwwwwwwwwwbo.",
  "..bbbbbbbbbbbb..",
  "................",
  "................"
)

$passport = @(
  "................",
  "...oooooooo.....",
  "...oiiiiiio.....",
  "...oiIIIIio.....",
  "...oiiyyiio.....",
  "...oiyYYyio.....",
  "...oiyYYyio.....",
  "...oiiyyiio.....",
  "...oiiiiiio.....",
  "...oiIIIIio.....",
  "...oiiiiiio.....",
  "...oiyyyyio.....",
  "...oiiiiiio.....",
  "...oooooooo.....",
  "................",
  "................"
)

$treasury_note = @(
  "................",
  "................",
  "..oooooooooooo..",
  "..oeeeeeeeeeeo..",
  "..oeEEEEEEEEeo..",
  "..oeeyoooyeeeo..",
  "..oeeoyyyoeeeo..",
  "..oeeooyooeeeo..",
  "..oeeoyyyoeeeo..",
  "..oeeoooyoeeeo..",
  "..oeeyoooyeeeo..",
  "..oeEEEEEEEEeo..",
  "..oeeeeeeeeeeo..",
  "..oooooooooooo..",
  "................",
  "................"
)

$coin_pouch = @(
  "................",
  "................",
  ".....oooo.......",
  ".....obbo.......",
  "....oobboo......",
  "...obbbbbbo.....",
  "..obbyyyybbo....",
  "..obyYYYYybo....",
  "..obyYyyYybo....",
  "..obyYYYYybo....",
  "..obbyyyybbo....",
  "...obbbbbbo.....",
  "....oobboo......",
  ".....oooo.......",
  "................",
  "................"
)

$dev_menu = @(
  "................",
  "......oggo......",
  "...o..ogo..o....",
  "..ogo.ogo.ogo...",
  ".. oggoooooggo .",
  "...ogGGGGGGgo...",
  "..oogGckkcGgoo..",
  "..ogGckGGkcGgo..",
  "..ogGckGGkcGgo..",
  "..oogGckkcGgoo..",
  "...ogGGGGGGgo...",
  "..ogo.ogo.ogo...",
  "...o..ogo..o....",
  "......oggo......",
  "................",
  "................"
)

Save-Icon "crown" $crown
Save-Icon "gavel" $gavel
Save-Icon "cursed_blade" $cursed_blade
Save-Icon "cursed_dagger" $cursed_dagger
Save-Icon "cursed_greatsword" $cursed_greatsword
Save-Icon "cursed_polearm" $cursed_polearm
Save-Icon "cursed_whip" $cursed_whip
Save-Icon "soul_cleaver" $soul_cleaver
Save-Icon "ballot" $ballot
Save-Icon "decree_scroll" $decree_scroll
Save-Icon "passport" $passport
Save-Icon "treasury_note" $treasury_note
Save-Icon "coin_pouch" $coin_pouch
Save-Icon "dev_menu" $dev_menu

# Paint horns onto the existing 64x64 curse skin (UV region x56..63, y0..5) so the
# model's horn cubes aren't untextured.
$curse = "src\main\resources\assets\politicalserver\textures\entity\curse_spirit.png"
if (Test-Path $curse) {
  # Load via a byte[]/MemoryStream so the source PNG file isn't locked while we re-save it.
  $bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $curse).Path)
  $ms = New-Object System.IO.MemoryStream(,$bytes)
  $src = [System.Drawing.Image]::FromStream($ms)
  $img = New-Object System.Drawing.Bitmap $src
  $src.Dispose(); $ms.Dispose()
  $dk = [System.Drawing.Color]::FromArgb(255,70,22,95)
  $lt = [System.Drawing.Color]::FromArgb(255,150,50,205)
  for ($x=56; $x -lt 64; $x++) {
    for ($y=0; $y -lt 6; $y++) {
      if ($y -lt 2) { $col = $lt } else { $col = $dk }
      $img.SetPixel($x, $y, $col)
    }
  }
  $img.Save($curse, [System.Drawing.Imaging.ImageFormat]::Png)
  $img.Dispose()
  Write-Host "painted horns onto $curse"
}
Write-Host "done"
