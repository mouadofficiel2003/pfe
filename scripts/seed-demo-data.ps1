# Demo data: concours, centres, etablissements, salles.
# Requires: auth (8081), concours (8083), lieux (8084).
# Usage: .\scripts\seed-demo-data.ps1

$ErrorActionPreference = "Stop"

$AuthBase = "http://localhost:8081"
$ConcoursBase = "http://localhost:8083"
$LieuxBase = "http://localhost:8084"

function Wait-ServicesReady {
    param([int]$MaxSeconds = 120)
    $deadline = (Get-Date).AddSeconds($MaxSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $loginBody = '{"username":"gestionnaire","password":"Gest123!"}'
            $login = Invoke-RestMethod -Method POST -Uri "$AuthBase/auth/login" -ContentType "application/json" -Body $loginBody
            $t = $login.accessToken
            $h = @{ Authorization = "Bearer $t" }
            Invoke-WebRequest -Uri "$ConcoursBase/api/concours" -Headers $h -Method GET -UseBasicParsing | Out-Null
            Invoke-WebRequest -Uri "$LieuxBase/api/centres" -Headers $h -Method GET -UseBasicParsing | Out-Null
            return @{ Token = $t; Headers = $h }
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    throw "Services not ready. Start auth, concours, lieux then retry."
}

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory)][string]$Method,
        [Parameter(Mandatory)][string]$Url,
        [Parameter(Mandatory)][hashtable]$Headers,
        $Body = $null
    )
    $params = @{
        Method  = $Method
        Uri     = $Url
        Headers = $Headers
    }
    if ($null -ne $Body) {
        $params.ContentType = "application/json; charset=utf-8"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 10 -Compress))
    }
    $resp = Invoke-WebRequest @params -UseBasicParsing
    if ([string]::IsNullOrWhiteSpace($resp.Content)) {
        return $null
    }
    return $resp.Content | ConvertFrom-Json
}

Write-Host "Waiting for services..." -ForegroundColor Cyan
$auth = Wait-ServicesReady
$authHeaders = $auth.Headers
Write-Host "Logged in as gestionnaire." -ForegroundColor Green

$centreSpecs = @("Centre Rabat", "Centre Casablanca", "Centre Fes")
$centreIds = @{}

$existingCentres = @(Invoke-ApiJson GET "$LieuxBase/api/centres" $authHeaders)
if ($null -eq $existingCentres) { $existingCentres = @() }

foreach ($nom in $centreSpecs) {
    $found = $existingCentres | Where-Object { $_.nomCentre -eq $nom } | Select-Object -First 1
    if ($found) {
        $centreIds[$nom] = [long]$found.id
        Write-Host "Centre exists: $nom (id=$($found.id))" -ForegroundColor DarkYellow
    } else {
        $created = Invoke-ApiJson POST "$LieuxBase/api/centres" $authHeaders @{ nomCentre = $nom }
        $centreIds[$nom] = [long]$created.id
        Write-Host "Centre created: $nom (id=$($created.id))" -ForegroundColor Green
        $existingCentres += $created
    }
}

$concoursSpecs = @(
    @{
        nomConcours     = "Concours National Enseignement"
        numeroConcours  = "ENC-2026-01"
        dateHeureExamen = "2026-06-15T08:00:00Z"
        centres         = @(
            @{ nomCentre = "Centre Rabat"; centreId = $centreIds["Centre Rabat"] },
            @{ nomCentre = "Centre Casablanca"; centreId = $centreIds["Centre Casablanca"] }
        )
    },
    @{
        nomConcours     = "Concours Sante Publique"
        numeroConcours  = "CSP-2026-02"
        dateHeureExamen = "2026-06-20T13:00:00Z"
        centres         = @(
            @{ nomCentre = "Centre Rabat"; centreId = $centreIds["Centre Rabat"] },
            @{ nomCentre = "Centre Fes"; centreId = $centreIds["Centre Fes"] }
        )
    },
    @{
        nomConcours     = "Concours Administration"
        numeroConcours  = "CAD-2026-03"
        dateHeureExamen = "2026-06-18T08:00:00Z"
        centres         = @(
            @{ nomCentre = "Centre Casablanca"; centreId = $centreIds["Centre Casablanca"] },
            @{ nomCentre = "Centre Fes"; centreId = $centreIds["Centre Fes"] }
        )
    }
)

$concoursIds = @{}
$allConcours = @(Invoke-ApiJson GET "$ConcoursBase/api/concours" $authHeaders)
if ($null -eq $allConcours -or $allConcours.Count -eq 0) { $allConcours = @() }

foreach ($spec in $concoursSpecs) {
    $found = $allConcours | Where-Object { $_.numeroConcours -eq $spec.numeroConcours } | Select-Object -First 1
    if ($found) {
        $concoursIds[$spec.numeroConcours] = [long]$found.id
        Write-Host "Concours exists: $($spec.nomConcours) (id=$($found.id))" -ForegroundColor DarkYellow
    } else {
        $created = Invoke-ApiJson POST "$ConcoursBase/api/concours" $authHeaders $spec
        $concoursIds[$spec.numeroConcours] = [long]$created.id
        Write-Host "Concours created: $($spec.nomConcours) (id=$($created.id))" -ForegroundColor Green
        $allConcours = @(Invoke-ApiJson GET "$ConcoursBase/api/concours" $authHeaders)
    }
}

$lieuxTree = @(
    @{
        Centre = "Centre Rabat"
        Etablissements = @(
            @{
                Nom = "Faculte des Sciences Rabat"
                Salles = @(
                    @{ Nom = "Amphi A"; Places = 120; ConcoursNum = "ENC-2026-01" },
                    @{ Nom = "Salle B12"; Places = 40; ConcoursNum = "ENC-2026-01" },
                    @{ Nom = "Salle C03"; Places = 35; ConcoursNum = "CSP-2026-02" }
                )
            },
            @{
                Nom = "Lycee Hassan II"
                Salles = @(
                    @{ Nom = "Salle 101"; Places = 30; ConcoursNum = "CSP-2026-02" },
                    @{ Nom = "Salle 102"; Places = 30; ConcoursNum = "ENC-2026-01" }
                )
            }
        )
    },
    @{
        Centre = "Centre Casablanca"
        Etablissements = @(
            @{
                Nom = "ENCG Casablanca"
                Salles = @(
                    @{ Nom = "Amphi Principal"; Places = 200; ConcoursNum = "ENC-2026-01" },
                    @{ Nom = "Salle 201"; Places = 45; ConcoursNum = "CAD-2026-03" }
                )
            },
            @{
                Nom = "Lycee Ibn Khaldoun"
                Salles = @(
                    @{ Nom = "Salle A1"; Places = 28; ConcoursNum = "CAD-2026-03" },
                    @{ Nom = "Salle A2"; Places = 28; ConcoursNum = "ENC-2026-01" }
                )
            }
        )
    },
    @{
        Centre = "Centre Fes"
        Etablissements = @(
            @{
                Nom = "Universite Sidi Mohammed Ben Abdellah"
                Salles = @(
                    @{ Nom = "Amphi Sud"; Places = 150; ConcoursNum = "CSP-2026-02" },
                    @{ Nom = "Labo L1"; Places = 25; ConcoursNum = "CSP-2026-02" }
                )
            },
            @{
                Nom = "Lycee Qualifiant Fes"
                Salles = @(
                    @{ Nom = "Salle 12"; Places = 32; ConcoursNum = "CAD-2026-03" },
                    @{ Nom = "Salle 14"; Places = 32; ConcoursNum = "CAD-2026-03" }
                )
            }
        )
    }
)

foreach ($block in $lieuxTree) {
    $centreId = $centreIds[$block.Centre]
    $centreDetail = Invoke-ApiJson GET "$LieuxBase/api/centres/$centreId" $authHeaders

    foreach ($etabSpec in $block.Etablissements) {
        $etab = @($centreDetail.etablissements) | Where-Object { $_.nomEtablissement -eq $etabSpec.Nom } | Select-Object -First 1
        if (-not $etab) {
            $etab = Invoke-ApiJson POST "$LieuxBase/api/centres/$centreId/etablissements" $authHeaders @{
                nomEtablissement = $etabSpec.Nom
            }
            Write-Host "Etablissement created: $($etabSpec.Nom) in $($block.Centre)" -ForegroundColor Green
        } else {
            Write-Host "Etablissement exists: $($etabSpec.Nom)" -ForegroundColor DarkYellow
        }
        $etabId = [long]$etab.id

        foreach ($salleSpec in $etabSpec.Salles) {
            $concoursId = $concoursIds[$salleSpec.ConcoursNum]
            $salles = @($etab.salles)
            if ($null -eq $salles) { $salles = @() }
            $existingSalle = $salles | Where-Object { $_.nomSalle -eq $salleSpec.Nom } | Select-Object -First 1
            if ($existingSalle) {
                Write-Host "  Salle exists: $($salleSpec.Nom)" -ForegroundColor DarkGray
                continue
            }
            Invoke-ApiJson POST "$LieuxBase/api/etablissements/$etabId/salles" $authHeaders @{
                nomSalle     = $salleSpec.Nom
                nombrePlaces = $salleSpec.Places
                concoursId   = $concoursId
            } | Out-Null
            Write-Host "  Salle created: $($salleSpec.Nom) ($($salleSpec.Places) places, $($salleSpec.ConcoursNum))" -ForegroundColor Green
        }
        $centreDetail = Invoke-ApiJson GET "$LieuxBase/api/centres/$centreId" $authHeaders
    }
}

Write-Host ""
Write-Host "Done. Open http://localhost:5173/lieux and http://localhost:5173/concours" -ForegroundColor Cyan
Write-Host "3 centres | 3 concours | 6 etablissements | 14 salles"
