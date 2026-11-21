final class StructureViewer {
    static String html(String data) {
        return """
                <!doctype html>
                <html lang="sq">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Protein folding · Pamja 3D</title>
                <style>
                :root{color-scheme:dark;font-family:system-ui,-apple-system,"Segoe UI",sans-serif;color:#e8eef6;background:#0b1019}
                *{box-sizing:border-box}body{margin:0;padding:36px;max-width:1600px;margin-inline:auto}
                header{display:flex;justify-content:space-between;align-items:center;gap:24px;margin-bottom:26px}
                .eyebrow{color:#63ddbd;font-size:12px;letter-spacing:2px;font-weight:700;margin:0 0 8px}
                h1{font-size:32px;letter-spacing:-1px;margin:0}header p{color:#95a5bb;margin:9px 0 0}
                .badge{border:1px solid #294039;color:#8de4cb;border-radius:30px;padding:8px 15px;white-space:nowrap;font-size:13px}
                main{display:grid;grid-template-columns:290px minmax(0,1fr);gap:20px}
                aside,.viewer{background:#111a28;border:1px solid #263346;border-radius:18px}
                aside{padding:24px;align-self:start}label,.section-title{display:block;font-size:12px;color:#a4b2c5;letter-spacing:1px;margin:0 0 10px}
                select,button,a{font:inherit}select{width:100%;padding:11px;border:1px solid #425169;background:#192537;color:#f1f5fa;border-radius:9px}
                .metrics{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin:22px 0}
                .metric{padding:13px 11px;background:#192436;border-radius:10px;min-width:0}.metric span{font-size:12px;color:#a8b6c9;display:block}
                .metric strong{display:block;font-size:24px;overflow-wrap:anywhere;margin-top:3px}.metric.energy strong{color:#63ddbd}
                #sequence{font-family:ui-monospace,Consolas,monospace;letter-spacing:3px;line-height:1.7;overflow-wrap:anywhere;margin:0 0 22px}
                .legend{display:grid;gap:12px;color:#c1cddd;font-size:13px}.legend div{display:flex;align-items:center;gap:10px}
                .dot{width:13px;height:13px;border-radius:50%;display:inline-block}.h{background:#44d5b2}.p{background:#b7a0ff}
                .line{width:24px;border-top:3px solid #a4b6ca}.contact{border-top:2px dashed #f3bb60}
                a{color:#82e4ca;display:inline-block;margin-top:24px;text-underline-offset:4px;font-size:13px}
                .viewer{overflow:hidden}.toolbar{display:flex;justify-content:space-between;align-items:center;padding:17px 22px;border-bottom:1px solid #263346;gap:10px}
                #algorithm{font-weight:650}button{background:#1c2a3e;color:#eef5fc;border:1px solid #425169;border-radius:8px;padding:8px 12px;cursor:pointer}
                button:hover{background:#2a3b53}button:focus-visible,select:focus-visible,canvas:focus-visible,a:focus-visible{outline:2px solid #63ddbd;outline-offset:3px}
                .canvas-wrap{height:560px;position:relative;background:radial-gradient(ellipse at center,#17263a 0%,#0d1522 75%)}
                canvas{display:block;width:100%;height:100%;touch-action:none;cursor:grab}canvas:active{cursor:grabbing}
                .help{padding:15px 22px;color:#a4b2c5;font-size:12px;border-top:1px solid #263346;display:flex;justify-content:space-between;gap:12px}
                #notice{margin:20px 0 0;padding:15px 18px;border:1px solid #635238;border-radius:12px;background:#29251d;color:#efcb8e;font-size:14px}
                footer{font-size:12px;color:#8395ad;margin-top:22px;line-height:1.6}
                @media(max-width:800px){body{padding:18px}header{align-items:flex-start}.badge{display:none}main{grid-template-columns:1fr}aside{padding:20px}.canvas-wrap{height:420px}.metrics{margin:16px 0}.legend{grid-template-columns:1fr 1fr}.help{flex-wrap:wrap}h1{font-size:27px}}
                </style>
                </head>
                <body>
                <header><div><p class="eyebrow">STRUKTURA NË RRJETËN KUBIKE</p><h1>Protein folding</h1><p>Eksploro strukturën e gjetur, nga çdo kënd.</p></div><span class="badge">Pamje interaktive 3D</span></header>
                <main>
                <aside>
                <label for="result">REZULTATI</label><select id="result" aria-label="Zgjidh algoritmin"></select>
                <div class="metrics">
                <div class="metric"><span>Pozicione</span><strong id="count"></strong></div>
                <div class="metric"><span>Kontakte H–H</span><strong id="contacts"></strong></div>
                <div class="metric"><span>Pesha H–H</span><strong id="weight"></strong></div>
                <div class="metric energy"><span>Energjia</span><strong id="energy"></strong></div>
                </div>
                <p class="section-title">SEKUENCA HP</p><p id="sequence"></p>
                <p class="section-title">LEGJENDA</p>
                <div class="legend"><div><i class="dot h"></i>H · hidrofob</div><div><i class="dot p"></i>P · polar</div><div><i class="line"></i>Lidhje e zinxhirit</div><div><i class="line contact"></i>Kontakt shtesë H–H</div></div>
                <a id="csv" download>Shkarko koordinatat CSV</a>
                </aside>
                <section class="viewer" aria-label="Pamja tredimensionale e proteinës">
                <div class="toolbar"><span id="algorithm"></span><button id="reset" type="button">Rikthe pamjen</button></div>
                <div class="canvas-wrap"><canvas id="structure" tabindex="0" aria-label="Struktura 3D. Rrotullo me maus ose shigjeta; zmadho me rrotë ose plus dhe minus."></canvas></div>
                <div class="help"><span>Tërhiq për rrotullim · Rrota për zmadhim</span><span id="bond-count"></span></div>
                </section>
                </main>
                <p id="notice" hidden></p>
                <footer>Numrat pranë pikave janë indekset e sekuencës, duke filluar nga 0. Rrotullimi ndryshon vetëm pamjen; koordinatat në CSV mbeten origjinale.</footer>
                <noscript><p>Aktivizo JavaScript në shfletues për të përdorur pamjen interaktive.</p></noscript>
                <script id="protein-data" type="application/json">__PROTEIN_DATA__</script>
                <script>
                const data = JSON.parse(document.getElementById('protein-data').textContent);
                const select = document.getElementById('result');
                const canvas = document.getElementById('structure');
                const ctx = canvas.getContext('2d');
                const byId = id => document.getElementById(id);
                let current, center, radius, yaw, pitch, zoom, dragging = false, lastX = 0, lastY = 0;
                let width = 1, height = 1;
                data.structures.forEach((structure, index) => select.add(new Option(structure.name, index)));
                if (data.missingBeamReason) {
                    byId('notice').textContent = data.missingBeamReason;
                    byId('notice').hidden = false;
                }
                function resetView() { yaw = -0.55; pitch = 0.45; zoom = 1; draw(); }
                function chooseResult() {
                    current = data.structures[Number(select.value)];
                    byId('algorithm').textContent = current.name;
                    byId('count').textContent = current.positions.length;
                    byId('contacts').textContent = current.contacts.length;
                    byId('weight').textContent = data.contactWeight;
                    byId('energy').textContent = current.energy;
                    byId('sequence').textContent = data.sequence;
                    byId('bond-count').textContent = current.bonds.length + ' lidhje · ' + current.contacts.length + ' kontakte';
                    byId('csv').href = current.csv;
                    const minimum = [Infinity, Infinity, Infinity], maximum = [-Infinity, -Infinity, -Infinity];
                    current.positions.forEach(p => p.forEach((value, axis) => {
                        minimum[axis] = Math.min(minimum[axis], value);
                        maximum[axis] = Math.max(maximum[axis], value);
                    }));
                    center = minimum.map((value, axis) => (value + maximum[axis]) / 2);
                    radius = 1;
                    current.positions.forEach(p => { radius = Math.max(radius, Math.hypot(...p.map((v, i) => v - center[i]))); });
                    resetView();
                }
                function rotate(x, y, z) {
                    const a = x * Math.cos(yaw) + z * Math.sin(yaw);
                    const b = -x * Math.sin(yaw) + z * Math.cos(yaw);
                    return [a, y * Math.cos(pitch) - b * Math.sin(pitch), y * Math.sin(pitch) + b * Math.cos(pitch)];
                }
                function project(position) {
                    const [x, y, z] = rotate(...position.map((v, i) => (v - center[i]) / radius));
                    const perspective = 4 / (4 - z);
                    const scale = Math.min(width, height) * 0.29 * zoom;
                    return {x: width / 2 + x * scale * perspective, y: height / 2 - y * scale * perspective, z, perspective};
                }
                function draw() {
                    if (!current) return;
                    ctx.clearRect(0, 0, width, height);
                    const projected = current.positions.map(project);
                    const objects = [];
                    function addEdges(pairs, contact) {
                        pairs.forEach(([i, j]) => objects.push({kind: 'edge', a: projected[i], b: projected[j], contact, z: (projected[i].z + projected[j].z) / 2}));
                    }
                    addEdges(current.bonds, false);
                    addEdges(current.contacts, true);
                    projected.forEach((p, i) => objects.push({kind: 'node', p, i, z: p.z}));
                    objects.sort((a, b) => a.z - b.z);
                    objects.forEach(object => {
                        if (object.kind === 'edge') {
                            ctx.beginPath(); ctx.setLineDash(object.contact ? [7, 6] : []);
                            ctx.strokeStyle = object.contact ? '#f3bb60' : '#90a6c1';
                            ctx.lineWidth = object.contact ? 2 : 4;
                            ctx.moveTo(object.a.x, object.a.y); ctx.lineTo(object.b.x, object.b.y); ctx.stroke();
                        } else {
                            const {p, i} = object;
                            const size = Math.max(6, Math.min(17, 12 * p.perspective * Math.sqrt(zoom)));
                            ctx.setLineDash([]); ctx.beginPath(); ctx.arc(p.x, p.y, size, 0, 2 * Math.PI);
                            ctx.fillStyle = data.sequence[i] === 'H' ? '#44d5b2' : '#b7a0ff';
                            ctx.fill(); ctx.strokeStyle = '#d3f8f1'; ctx.lineWidth = 1; ctx.stroke();
                            ctx.font = '600 12px system-ui'; ctx.textAlign = 'left';
                            ctx.lineWidth = 4; ctx.strokeStyle = '#101b2a'; ctx.strokeText(String(i), p.x + size + 5, p.y - size);
                            ctx.fillStyle = '#edf4fc'; ctx.fillText(String(i), p.x + size + 5, p.y - size);
                        }
                    });
                    // Boshtet ndjekin te njejtin rrotullim si struktura.
                    [['X', '#f58e91', [1,0,0]], ['Y', '#74dfad', [0,1,0]], ['Z', '#8cb9ff', [0,0,1]]].forEach(([label, color, axis]) => {
                        const [x, y] = rotate(...axis), ox = width - 62, oy = height - 58;
                        ctx.setLineDash([]); ctx.beginPath(); ctx.moveTo(ox, oy); ctx.lineTo(ox + x * 31, oy - y * 31);
                        ctx.strokeStyle = color; ctx.lineWidth = 2; ctx.stroke();
                        ctx.fillStyle = color; ctx.font = '12px system-ui'; ctx.fillText(label, ox + x * 42 - 3, oy - y * 42 + 4);
                    });
                }
                function resize() {
                    const rect = canvas.getBoundingClientRect(), dpr = window.devicePixelRatio || 1;
                    width = rect.width; height = rect.height;
                    canvas.width = Math.round(width * dpr); canvas.height = Math.round(height * dpr);
                    ctx.setTransform(dpr, 0, 0, dpr, 0, 0); draw();
                }
                canvas.addEventListener('pointerdown', event => {
                    dragging = true; lastX = event.clientX; lastY = event.clientY; canvas.setPointerCapture(event.pointerId); canvas.focus();
                });
                canvas.addEventListener('pointermove', event => {
                    if (!dragging) return;
                    yaw += (event.clientX - lastX) * 0.01;
                    pitch = Math.max(-1.45, Math.min(1.45, pitch + (event.clientY - lastY) * 0.01));
                    lastX = event.clientX; lastY = event.clientY; draw();
                });
                canvas.addEventListener('pointerup', () => { dragging = false; });
                canvas.addEventListener('pointercancel', () => { dragging = false; });
                canvas.addEventListener('wheel', event => {
                    event.preventDefault(); zoom = Math.max(0.25, Math.min(5, zoom * Math.exp(-event.deltaY * 0.001))); draw();
                }, {passive:false});
                canvas.addEventListener('keydown', event => {
                    if (!['ArrowLeft','ArrowRight','ArrowUp','ArrowDown','+','=','-','Home'].includes(event.key)) return;
                    event.preventDefault();
                    if (event.key === 'Home') { resetView(); return; }
                    if (event.key === 'ArrowLeft') yaw -= 0.15;
                    if (event.key === 'ArrowRight') yaw += 0.15;
                    if (event.key === 'ArrowUp') pitch -= 0.15;
                    if (event.key === 'ArrowDown') pitch += 0.15;
                    if (event.key === '+' || event.key === '=') zoom *= 1.15;
                    if (event.key === '-') zoom /= 1.15;
                    zoom = Math.max(0.25, Math.min(5, zoom)); pitch = Math.max(-1.45, Math.min(1.45, pitch)); draw();
                });
                select.addEventListener('change', chooseResult);
                byId('reset').addEventListener('click', resetView);
                chooseResult(); new ResizeObserver(resize).observe(canvas);
                </script>
                </body>
                </html>
                """.replace("__PROTEIN_DATA__", data);
    }
}
