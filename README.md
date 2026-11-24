# AI-Based Protein Structure Prediction and Optimization

Program për kërkimin e strukturave proteinike me energji të ulët, me Simulated Annealing dhe Beam Search. Përdor vetëm bibliotekën standarde.

## Problemi dhe modeli HP

**H** përfaqëson kategorinë hidrofobe, ndërsa **P** kategorinë polare. Ky është një model i thjeshtuar HP. Konvertuesi i aminoacideve përdor konventën lokale `AVILMFWYC → H`; aminoacidet e tjera standarde bëhen P. Prandaj `HP` si aminoacide bëhet `PP`.

Zinxhiri vendoset në koordinata të plota `(x,y,z)` në rrjetën kubike 3D. Është i vlefshëm kur nuk është bosh, nuk ka mbivendosje dhe pozicionet e njëpasnjëshme kanë distancë Manhattan 1. `isComplete()` kërkon edhe që të jenë vendosur të gjitha pozicionet e sekuencës.

**E = −pesha × numri i kontakteve shtesë H–H**

Kontakti numërohet një herë, midis dy H-ve fqinje që nuk janë të njëpasnjëshme në zinxhir. Pesha është numër i plotë pozitiv; energjia e peshuar ruhet si `long`.

Shembull: `HHHH` në `(0,0,0) → (1,0,0) → (1,1,0) → (0,1,0)` ka **tri lidhje zinxhiri**, por vetëm **një kontakt shtesë, 0–3**. Me peshë 1 energjia është **−1**; me peshë 2 është **−2**.

## Algoritmet

- **Simulated Annealing (SA)** nis nga një strukturë e plotë, në vijë në Main/ExperimentRunner. Provon në kopje zhvendosjen e aminoacidit të fundit ose një rrotullim pivot. Përmirësimet/barazimet pranohen; përkeqësimet pranohen me probabilitet `exp(−ΔE/T)` kur `T > 0`, ndërsa në temperaturë zero refuzohen. Ftohja bëhet pas çdo përpjekjeje, edhe pa kandidat. Kthehet `best`, struktura më e mirë e gjetur, jo domosdoshmërisht gjendja e fundit `current`.
- **Beam Search** vendos pikën e parë në origjinë dhe ndërton zinxhirin pozicion pas pozicioni. Bashkon zgjatjet nga të gjithë prindërit në gjashtë drejtimet dhe ruan deri në `beamWidth` kandidatë me energjinë më të ulët; barazimet ruajnë rendin fillestar. Heqja e kopjeve sipas 24 rrotullimeve liron vende për forma të ndryshme, pa përfshirë pasqyrimet ose kthimin mbrapsht të sekuencës.

Kufiri kohor është opsional dhe i veçantë për secilin kërkim; hapi i nisur përfundon. SA kthen `best`; Beam pa strukturë të plotë kthen `null`. Regjistrohet arsyeja e ndalimit.

SA u zgjodh sepse mund të dalë nga një minimum lokal duke pranuar përkohësisht përkeqësime. Beam lejon krahasimin me një ndërtim determinist, ku gjerësia kontrollon sa degë ruhen. Asnjëri nuk garanton minimumin global në këtë implementim.

`ArrayList` ruan rendin e aminoacideve dhe të kandidatëve, lejon qasje sipas indeksit dhe renditjen e qëndrueshme. `HashSet` përdoret vetëm për çelësat e formave të rrotulluara në Beam: zbulon shpejt kopjet, ndërsa një listë më vete ruan rendin e kandidatëve të parë. Validimi gjeometrik vazhdon me cikle të thjeshta.

Për konvergjencën krahasojmë energjinë më të mirë të **strukturës së plotë** të SA pas disa numrash iteracionesh. Beam ndërton **struktura të pjesshme**, një nivel pas tjetrit. Energjia e prefiksit nuk është e barasvlershme me rezultatin final; krahasimi mes algoritmeve përdor vetëm strukturat e plota.

## Kompleksiteti sipas implementimit

`n` = gjatësia e sekuencës, `I` = përpjekjet e SA, `B` = gjerësia e Beam. Kufijtë vlejnë pa kufi kohor; hapësira tregon objektet aktive, pa eksportin CSV/HTML.

| Algoritmi | Koha | Hapësira | Nga vjen kostoja |
|---|---|---|---|
| Simulated Annealing | `O((I + 1) n²)` | `O(n)` | Inicializimi dhe çdo përpjekje kanë validim/energji `O(n²)` dhe kopje `O(n)`. Ruhen disa struktura, pa histori iteracionesh. |
| Beam Search | `O(B n³ + n B log(B + 1))` | `O(B n)` | Në nivelin me `k` pozicione ka deri në `6B` kandidatë: validimi/energjia `O(B k²)`, renditja `O(B log(B + 1))`. Mblidhen `n` nivele; ruhen beam-i dhe kandidatët pasues. |

Kopja e pjesshme kontrollon edhe sekuencën e plotë: `O(n + k)` për kopje, deri në `O(B n²)` gjithsej, brenda kufirit të Beam. Çelësi i 24 rrotullimeve kushton `O(k)` për kandidat; tabela supozon sjelljen mesatare të kërkimit në `HashSet`. Çelësat zënë deri në `O(B n)` hapësirë.

Kufijtë teorikë tregojnë rritjen e punës. Milisekondat maten me `System.nanoTime()` vetëm rreth kërkimit dhe ndikohen nga JVM-ja, GC-ja dhe kompjuteri; nuk janë kohë fikse të garantuara.

## Si lexohen metrikat

- **SA:** një propozim është një përpjekje për lëvizje; struktura fillestare nuk numërohet. Kandidati i vlefshëm por i refuzuar nga temperatura mbetet i vlefshëm. Përplasja/lidhja e këputur, lëvizja pa ndryshim, mungesa e lëvizjes (p.sh. një aminoacid) dhe tejkalimi i kufijve `int` numërohen veçmas. `attempts = valid_candidates + geometrically_invalid_proposals + unchanged_proposals + no_move_proposals + out_of_range_proposals`. Këto janë ngjarje, jo numër formash unike.
- **Beam:** një propozim është njëri nga gjashtë drejtimet e provuara për çdo prind. Vendosja e parë në origjinë numërohet si një propozim i vlefshëm. Zgjatja mbi një pozicion të zënë është gjeometrikisht e pavlefshme; drejtimi jashtë kufijve `int` raportohet veçmas. `proposals = generated_candidates + collision_proposals + out_of_range_proposals`. Kandidatët e gjeneruar përfshijnë prefikset e vlefshme, edhe kur më pas hiqen si kopje ose nga gjerësia.
- **Përqindja e kandidatëve të gjeneruar që u eliminuan:** për Beam është `100 × (rotated_duplicates + pruned_candidates) / generated_candidates`, mbi të gjitha nivelet e përfunduara; emëruesi zero shfaqet `N/A`. Nuk është pjesa e eliminuar e gjithë hapësirës teorike dhe nuk zbatohet për refuzimet e temperaturës në SA. Për `HH`, me heqjen e rrotullimeve: niveli i dytë ka 6 kandidatë dhe 5 kopje; bashkë me origjinën ka 7 kandidatë gjithsej, ndaj përqindja totale është `5/7 × 100 ≈ 71.43%`.
- **Heap i përdorur i JVM-së para/pas kërkimit (MiB):** të dy algoritmet përdorin dy lexime me `MemoryMXBean`, përreth së njëjtës thirrje që matet për kohën. `1 MiB = 1 048 576 byte`. Leximet nuk përfshijnë eksportin/grafikët në intervalin e kërkimit, por heap-i është i përbashkët me programin dhe mund të përmbajë objekte që presin GC. As dy leximet, as diferenca e tyre nuk janë piku ose memoria ekskluzive e algoritmit; vlera pas mund të jetë më e ulët. Nuk detyrohet GC.

Main dhe raporti shfaqin metrikat; CSV-ja ruan numëruesit dhe kolonat `heap_used_before_mib` / `heap_used_after_mib`. Maksimumi i kandidatëve në pritje mbetet tregues më vete për Beam, jo MiB. Më shumë përpjekje në SA rrisin punën pa ruajtur histori; më shumë kandidatë në Beam rrisin punën dhe hapësirën aktive. Matjet konkrete ilustrojnë këto kosto, por nuk zëvendësojnë kufijtë asimptotikë dhe nuk garantojnë se një konfigurim është gjithmonë më i shpejtë.

## Ekzekutimi në IntelliJ

Hape projektin me **Project SDK: JDK 26**, sipas konfigurimit aktual, dhe **Working directory** në rrënjë, pranë `src`. Shtyp ▶ pranë `main()`:

1. **[Main](src/Main.java)**: zgjidh `1` për HP, pastaj `HHHH`. Enter ruan 1000 përpjekje, temperaturë `2.0`, ftohje `0.995`, seed `2025` dhe gjerësi `30`. Te kufiri kohor Enter zgjedh pa kufi, te rrotullimet i aktivizon dhe te pesha zgjedh `1`. Në fund, `p` ruan eksportin; Enter/`j` përfundon pa skedarë.
2. **[ProteinTests](src/ProteinTests.java)**: nis kontrollet; gabimet shkaktojnë `AssertionError`. Testet e skedarëve përdorin dosje të përkohshme.
3. **[ExperimentRunner](src/ExperimentRunner.java)**: nise pa argumente. Pas dy kërkimeve ngrohjeje jashtë rezultateve, kryen 46 prova: gjatësi 4/12/24, pesë seed-e `2025–2029`, Beam 10/30, pa kufi dhe me 10 ms për kërkim. Përfshin rrotullimet ndezur/fikur dhe SA me 100/500/1000 përpjekje me të njëjtin seed. Pesha është 1.

Daljet krijohen në dosje të reja `run-*`, pa mbishkruar ekzekutimet e mëparshme:

| Nisja | Daljet relative nga rrënja |
|---|---|
| Main, kur zgjidhet `p` | `exports/run-*/structure.html`, `exports/run-*/sa.csv` dhe `exports/run-*/beam.csv` vetëm për rezultatet e plota e të vlefshme |
| ExperimentRunner | `experiments/run-*/results.csv` dhe `experiments/run-*/report.html` |

HTML-të hapen lokalisht pa internet/server. `structure.html` lejon rrotullim/zmadhim pa ndryshuar koordinatat; kur Beam mungon, shënon arsyen. **ExperimentRunner krijon `results.csv` dhe `report.html`**, me tabelë/grafikë nga CSV-ja. Beam pa strukturë të plotë ka energji bosh. Daljet `exports/` dhe `experiments/` përjashtohen nga Git.

## Rezultate nga një provë ekzistuese

Burimi: `experiments/run-20261123-124255-1358947000540973069/results.csv`, rreshtat `baseline`. Sekuenca `HHPHPHHPPHPHHHPHPPHHPPHP` ka **24 pozicione**; pesha **1**, pa kufi kohor, kopjet e rrotulluara hiqen te Beam. SA: 1000 përpjekje, `T=2.0`, ftohje `0.995`, seed-et `2025–2029`.

| Konfigurimi | Energjia | Maksimum kandidatësh në pritje |
|---|---:|---:|
| SA: më e mira / mediana nga 5 seed-e | −10 / −10 | — |
| Beam, gjerësi 10 | −11 | 49 |
| Beam, gjerësi 30 | −14 | 146 |

`max_candidates_before_selection` mat maksimumin e kandidatëve të një niveli para heqjes së kopjeve/përzgjedhjes. Kalimi **49 → 146** tregon listën më të madhe kur zgjerohet beam-i: **numër kandidatësh, jo MB**, pa matur gjithë memorien.

Në këtë provë, Beam 30 gjeti energjinë më të ulët. **−14 nuk është provuar si minimum global**; përfundimi vlen vetëm për këtë krahasim.
