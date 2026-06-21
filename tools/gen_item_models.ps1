# Writes item models + item-definition files pointing the modded items at their new
# custom textures (replacing the vanilla-model aliases).
$base = "src\main\resources\assets\politicalserver"
New-Item -ItemType Directory -Force -Path "$base\models\item" | Out-Null

$handheld = @("cursed_blade","cursed_dagger","cursed_greatsword","cursed_polearm","cursed_whip","soul_cleaver","gavel")
$generated = @("crown","ballot","decree_scroll","passport","treasury_note","coin_pouch","dev_menu")

function Write-Model($name, $parent) {
  $model = "{`n  `"parent`": `"$parent`",`n  `"textures`": {`n    `"layer0`": `"politicalserver:item/$name`"`n  }`n}`n"
  Set-Content -Path "$base\models\item\$name.json" -Value $model -NoNewline
  $def = "{`n  `"model`": {`n    `"type`": `"minecraft:model`",`n    `"model`": `"politicalserver:item/$name`"`n  }`n}`n"
  Set-Content -Path "$base\items\$name.json" -Value $def -NoNewline
  Write-Host "model+def $name ($parent)"
}

foreach ($n in $handheld) { Write-Model $n "minecraft:item/handheld" }
foreach ($n in $generated) { Write-Model $n "minecraft:item/generated" }
Write-Host "done"
