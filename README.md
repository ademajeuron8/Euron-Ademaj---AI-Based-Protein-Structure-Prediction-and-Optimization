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

## Kompleksiteti sipas implementimit

`n` = gjatësia e sekuencës, `I` = përpjekjet e SA, `B` = gjerësia e Beam. Kufijtë vlejnë pa kufi kohor; hapësira tregon objektet aktive, pa eksportin CSV/HTML.

| Algoritmi | Koha | Hapësira | Nga vjen kostoja |
|---|---|---|---|
| Simulated Annealing | `O((I + 1) n²)` | `O(n)` | Inicializimi dhe çdo përpjekje kanë validim/energji `O(n²)` dhe kopje `O(n)`. Ruhen disa struktura, pa histori iteracionesh. |
| Beam Search | `O(B n³ + n B log(B + 1))` | `O(B n)` | Në nivelin me `k` pozicione ka deri në `6B` kandidatë: validimi/energjia `O(B k²)`, renditja `O(B log(B + 1))`. Mblidhen `n` nivele; ruhen beam-i dhe kandidatët pasues. |

Kopja e pjesshme kontrollon edhe sekuencën e plotë: `O(n + k)` për kopje, deri në `O(B n²)` gjithsej, brenda kufirit të Beam. Çelësi i 24 rrotullimeve kushton `O(k)` për kandidat; tabela supozon sjelljen mesatare të kërkimit në `HashSet`. Çelësat zënë deri në `O(B n)` hapësirë.

Kufijtë teorikë tregojnë rritjen e punës. Milisekondat maten me `System.nanoTime()` vetëm rreth kërkimit dhe ndikohen nga JVM-ja, GC-ja dhe kompjuteri; nuk janë kohë fikse të garantuara.

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
