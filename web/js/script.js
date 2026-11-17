const url = "http://localhost:7070";
let listaProjetos = []; // Cache local dos projetos carregados da API

async function Cadastrar(event) {
    event.preventDefault();
    
    const nome = document.getElementById("nome").value;
    const descricao = document.getElementById("descricao").value;
    const categoria = document.getElementById("categoria").value;
    const status = document.getElementById("status").value;    
    const enviar = {
        nome,
        descricao,
        categoria,
        status,
    };

    await fetch(`${url}/api/projetos`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(enviar)
    });

    document.getElementById("formCadastro").reset();
    Exibir();
}

async function DeletarSelecionado() {
    const checkboxes = document.querySelectorAll(`.projetos-select:checked`);
    const ids = Array.from(checkboxes).map(checkbox => checkbox.id.split('-')[1]);

    let feedback = document.getElementById("feedback");
    feedback.textContent = "";
    
    if (ids.length === 0) {
        feedback.textContent = "Nenhum projeto selecionado para exclusão.";
        return;
    }

    let confirmar = confirm("Deseja realmente deletar os projetos selecionados?");

    if (!confirmar) {
        feedback.textContent = "Exclusão cancelada.";
        return;
    }

    for (const id of ids) {
        await fetch(`${url}/api/projetos/${id}`, {
            method: "DELETE"
        });
    }

    Exibir();
}

async function CarregarSelecionado() {
    let feedback = document.getElementById("feedback");
    feedback.textContent = "";

    const checkboxes = document.querySelectorAll(`.projetos-select:checked`);
    const ids = Array.from(checkboxes).map(checkbox => checkbox.id.split('-')[1]);

    if (ids.length === 0) {
        feedback.textContent = "Nenhum projeto selecionado para edição.";
        return;
    }

    if (ids.length > 1) {
        feedback.textContent = "Selecione apenas um projeto para edição.";
        return;
    }

    const idSelecionado = ids[0];
    const projeto = listaProjetos.find(p => p.id == idSelecionado);

    if (!projeto) {
        feedback.textContent = "Projeto não encontrado.";
        return;
    }

    let elementoPai = document.getElementById("popup");
    elementoPai.innerHTML = `<section id="gestor-de-projetos" class="form-panel">
            <div class="section-heading">
                <div>
                    <p class="eyebrow">EDIÇÃO</p>
                    <h2>Editar projeto</h2>
                </div>
            </div>
            <form id="formEditar">
                <div class="field-grid">
                    <label class="field">
                        <span>Nome do projeto</span>
                        <input type="text" id="nomeEditar" name="nome" value="${projeto.nome}" required>
                    </label>
                    <label class="field">
                        <span>Descrição</span>
                        <input type="text" id="descricaoEditar" name="descricao" value="${projeto.descricao}" required>
                    </label>
                    <label class="field">
                        <span>Categoria</span>
                        <select id="categoriaEditar" name="categoria" required>
                            <option value="Web" ${projeto.categoria === "Web" ? "selected" : ""}>Web</option>
                            <option value="Software" ${projeto.categoria === "Software" ? "selected" : ""}>Software</option>
                            <option value="Mobile" ${projeto.categoria === "Mobile" ? "selected" : ""}>Mobile</option>
                            <option value="Desktop" ${projeto.categoria === "Desktop" ? "selected" : ""}>Desktop</option>
                            <option value="Outros" ${projeto.categoria === "Outros" ? "selected" : ""}>Outro</option>
                        </select>
                    </label>
                    <label class="field">
                        <span>Status</span>
                        <select id="statusEditar" name="status" required>
                            <option value="Concluído" ${projeto.status === "Concluído" ? "selected" : ""}>Concluído</option>
                            <option value="Em andamento" ${projeto.status === "Em andamento" ? "selected" : ""}>Em Desenv.</option>
                            <option value="Planejado" ${projeto.status === "Planejado" ? "selected" : ""}>Planejado</option>
                            <option value="Cancelado" ${projeto.status === "Cancelado" ? "selected" : ""}>Cancelado</option>
                        </select>
                    </label>
                </div>
                <div class="form-actions">
                    <button class="button button-quiet" type="reset">Limpar formulário</button>
                    <div class="button-group">
                        <button class="button button-primary" type="button" onclick="EditarSelecionado(${projeto.id})">Salvar Alterações</button>
                        <button class="button button-danger" type="button" onclick="FecharModal()">Cancelar</button>
                    </div>
                </div>
            </form>
        </section>`;

    const meuModal = document.getElementById("modalEditar");
    meuModal.style.display = "flex";
}

async function EditarSelecionado(id) {
    const nome = document.getElementById("nomeEditar").value;
    const descricao = document.getElementById("descricaoEditar").value;
    const categoria = document.getElementById("categoriaEditar").value;
    const status = document.getElementById("statusEditar").value;

    const enviar = {
        nome,
        descricao,
        categoria,
        status
    };

    await fetch(`${url}/api/projetos/${id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(enviar),
    });

    Exibir();
    FecharModal();
}

function FecharModal() {
    const meuModal = document.getElementById("modalEditar");
    meuModal.style.display = "none";
}

// === FUNÇÕES DE FILTRO E RENDERIZAÇÃO ===

async function Exibir() {
    try {
        const resposta = await fetch(`${url}/api/projetos`);
        if (resposta.ok) {
            listaProjetos = await resposta.json();
            AplicarFiltros(); // Mantém os filtros ativos mesmo após cadastrar/editar/excluir
            const modalGraficos = document.getElementById("modalGraficos");
            if (modalGraficos && modalGraficos.style.display === "flex") {
                RenderizarGraficos(listaProjetos);
            }
        }
    } catch (err) {
        console.error("Erro ao carregar projetos:", err);
    }
}

function AplicarFiltros() {
    const textoBusca = document.getElementById("campoBusca").value.toLowerCase().trim();
    const categoriaSelecionada = document.getElementById("filtroCategoria").value;
    const statusSelecionado = document.getElementById("filtroStatus").value;

    const projetosFiltrados = listaProjetos.filter(p => {
        const matchBusca = p.nome.toLowerCase().includes(textoBusca) ||
                           p.descricao.toLowerCase().includes(textoBusca);

        const matchCategoria = categoriaSelecionada === "todas" ||
                               p.categoria.toLowerCase() === categoriaSelecionada.toLowerCase();

        const matchStatus = statusSelecionado === "todos" ||
                            p.status.toLowerCase() === statusSelecionado.toLowerCase();

        return matchBusca && matchCategoria && matchStatus;
    });

    RenderizarTabela(projetosFiltrados);
}

function LimparFiltros() {
    document.getElementById("campoBusca").value = "";
    document.getElementById("filtroCategoria").value = "todas";
    document.getElementById("filtroStatus").value = "todos";
    AplicarFiltros();
}

function RenderizarTabela(projetos) {
    let corpo = document.getElementById("projetos-tbody");
    corpo.replaceChildren();

    if (projetos.length === 0) {
        corpo.insertAdjacentHTML(
            "beforeend",
            `<tr>
                <td colspan="6" style="text-align: center; color: var(--muted); padding: 24px;">
                    Nenhum projeto encontrado com os filtros aplicados.
                </td>
            </tr>`
        );
        return;
    }

    for (const projeto of projetos) {
        projeto.categoria = projeto.categoria.charAt(0).toUpperCase() + projeto.categoria.slice(1);
        projeto.status = projeto.status.charAt(0).toUpperCase() + projeto.status.slice(1);
        corpo.insertAdjacentHTML(
            "beforeend",
            `<tr>
                <td>${projeto.id}</td>
                <td>${projeto.nome}</td>
                <td>${projeto.descricao}</td>
                <td>${projeto.categoria}</td>
                <td>${projeto.status}</td>
                <td>
                    <input type="checkbox" id="projeto-${projeto.id}" name="projeto-${projeto.id}" class="projetos-select">
                    <label for="projeto-${projeto.id}"></label>
                </td>
            </tr>`
        );
    }
}

/* ========================================================
   FUNÇÕES DO DASHBOARD DE GRÁFICOS DE PIZZA
   ======================================================== */

async function AbrirModalGraficos() {
    const modal = document.getElementById("modalGraficos");
    if (!modal) return;
    modal.style.display = "flex";

    // Se já tivermos dados locais em cache, renderiza de imediato
    if (listaProjetos && listaProjetos.length > 0) {
        RenderizarGraficos(listaProjetos);
    }

    try {
        const resposta = await fetch(`${url}/api/projetos`);
        if (resposta.ok) {
            const projetos = await resposta.json();
            listaProjetos = projetos;
            RenderizarGraficos(projetos);
        }
    } catch (err) {
        console.error("Erro ao buscar dados para os gráficos:", err);
        if (listaProjetos) {
            RenderizarGraficos(listaProjetos);
        }
    }
}

function FecharModalGraficos() {
    const modal = document.getElementById("modalGraficos");
    if (modal) modal.style.display = "none";
}

function RenderizarGraficos(projetos) {
    if (!projetos) projetos = [];

    // D3: Categorias (Web, Software, Mobile, Desktop)
    const categorias = {
        "Web": 0,
        "Software": 0,
        "Mobile": 0,
        "Desktop": 0
    };

    // D2: Status (Concluído, Em Desenv., Planejado, Cancelado)
    const statusMap = {
        "Concluído": 0,
        "Em Desenv.": 0,
        "Planejado": 0,
        "Cancelado": 0
    };

    let concluidos = 0;
    let ativos = 0;
    let cancelados = 0;
    let planejados = 0;

    projetos.forEach(p => {
        // Normalização de categoria para D3
        let cat = normalizar(p.categoria);
        if (cat.includes("web")) {
            categorias["Web"] = (categorias["Web"] || 0) + 1;
        } else if (cat.includes("desk")) {
            categorias["Desktop"] = (categorias["Desktop"] || 0) + 1;
        } else if (cat.includes("mobi")) {
            categorias["Mobile"] = (categorias["Mobile"] || 0) + 1;
        } else if (cat.includes("soft")) {
            categorias["Software"] = (categorias["Software"] || 0) + 1;
        } else {
            categorias["Software"] = (categorias["Software"] || 0) + 1;
        }

        // Normalização de status para D1 e D2
        let st = normalizar(p.status);
        if (st.includes("conclu")) {
            concluidos++;
            statusMap["Concluído"] = (statusMap["Concluído"] || 0) + 1;
        } else if (st.includes("cancela")) {
            cancelados++;
            statusMap["Cancelado"] = (statusMap["Cancelado"] || 0) + 1;
        } else if (st.includes("andamento") || st.includes("desenv") || st.includes("ativo")) {
            ativos++;
            statusMap["Em Desenv."] = (statusMap["Em Desenv."] || 0) + 1;
        } else {
            planejados++;
            statusMap["Planejado"] = (statusMap["Planejado"] || 0) + 1;
        }
    });

    // D1: Atualizar Cards de KPI
    if (document.getElementById("kpiTotal")) document.getElementById("kpiTotal").textContent = projetos.length;
    if (document.getElementById("kpiConcluidos")) document.getElementById("kpiConcluidos").textContent = concluidos;
    if (document.getElementById("kpiAtivos")) document.getElementById("kpiAtivos").textContent = ativos;
    if (document.getElementById("kpiCancelados")) document.getElementById("kpiCancelados").textContent = cancelados;

    // Cores padronizadas para D3 (Barras - Categorias)
    const coresCat = {
        "Web": "#2563eb",
        "Software": "#176b51",
        "Mobile": "#7c3aed",
        "Desktop": "#0284c7"
    };

    // Cores padronizadas para D2 (Pizza - Status)
    const coresSt = {
        "Concluído": "#10b981",
        "Em Desenv.": "#3b82f6",
        "Planejado": "#8b5cf6",
        "Cancelado": "#ef4444"
    };

    DesenharPizza("canvasStatus", "legendaStatus", statusMap, coresSt);
    DesenharBarras("canvasCategorias", "legendaCategorias", categorias, coresCat);
}

function DesenharBarras(canvasId, legendaId, dados, mapaCores) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const legenda = document.getElementById(legendaId);

    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (legenda) legenda.innerHTML = "";

    const entries = Object.entries(dados);
    const total = entries.reduce((a, b) => a + b[1], 0);

    if (total === 0) {
        ctx.fillStyle = "#64766e";
        ctx.font = "14px 'DM Sans', sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText("Nenhum dado cadastrado", canvas.width / 2, canvas.height / 2);
        return;
    }

    const maxValor = Math.max(...entries.map(e => e[1]), 1);

    const xLabel = 10;
    const labelWidth = 72;
    const xInicioBarra = xLabel + labelWidth + 8;
    const paddingDir = 38;
    const larguraMaxBarra = canvas.width - xInicioBarra - paddingDir;

    const espacamento = 10;
    const numBarras = entries.length;
    const alturaDisponivel = canvas.height - 24;
    const alturaBarra = Math.min(28, Math.max(18, (alturaDisponivel - (numBarras - 1) * espacamento) / numBarras));

    let yAtual = 16;

    for (const [chave, valor] of entries) {
        const cor = mapaCores[chave] || "#2563eb";

        // Label da Categoria
        ctx.fillStyle = "#18342b";
        ctx.font = "bold 12px 'DM Sans', sans-serif";
        ctx.textAlign = "left";
        ctx.textBaseline = "middle";
        ctx.fillText(chave, xLabel, yAtual + alturaBarra / 2);

        // Fundo suave da barra
        ctx.fillStyle = "#f1f5f3";
        ctx.beginPath();
        if (ctx.roundRect) {
            ctx.roundRect(xInicioBarra, yAtual, larguraMaxBarra, alturaBarra, 4);
        } else {
            ctx.rect(xInicioBarra, yAtual, larguraMaxBarra, alturaBarra);
        }
        ctx.fill();

        // Barra preenchida proporcional
        const larguraBarra = Math.max(valor > 0 ? 8 : 0, (valor / maxValor) * larguraMaxBarra);
        if (larguraBarra > 0) {
            ctx.fillStyle = cor;
            ctx.beginPath();
            if (ctx.roundRect) {
                ctx.roundRect(xInicioBarra, yAtual, larguraBarra, alturaBarra, 4);
            } else {
                ctx.rect(xInicioBarra, yAtual, larguraBarra, alturaBarra);
            }
            ctx.fill();
        }

        // Valor numérico no final da barra (ex: Web: 35)
        ctx.fillStyle = "#18342b";
        ctx.font = "bold 12px 'Space Grotesk', sans-serif";
        ctx.textAlign = "left";
        ctx.fillText(`${valor}`, xInicioBarra + larguraMaxBarra + 8, yAtual + alturaBarra / 2);

        // Item na legenda
        if (legenda) {
            const item = document.createElement("div");
            item.style.display = "flex";
            item.style.alignItems = "center";
            item.style.justifyContent = "space-between";
            item.style.fontSize = "13px";
            item.style.padding = "2px 0";
            item.innerHTML = `
                <div style="display:flex; align-items:center; gap:8px;">
                    <span style="display:inline-block; width:10px; height:10px; border-radius:3px; background:${cor};"></span>
                    <strong style="color:var(--ink);">${chave}</strong>
                </div>
                <span style="color:var(--muted); font-weight:600;">${valor}</span>
            `;
            legenda.appendChild(item);
        }

        yAtual += alturaBarra + espacamento;
    }
}

function DesenharPizza(canvasId, legendaId, dados, mapaCores) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const legenda = document.getElementById(legendaId);

    const ctx = canvas.getContext("2d");
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    if (legenda) legenda.innerHTML = "";

    const total = Object.values(dados).reduce((a, b) => a + b, 0);

    if (total === 0) {
        ctx.fillStyle = "#64766e";
        ctx.font = "14px 'DM Sans', sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText("Nenhum dado cadastrado", canvas.width / 2, canvas.height / 2);
        return;
    }

    const centroX = canvas.width / 2;
    const centroY = canvas.height / 2;
    const raio = Math.min(centroX, centroY) - 10;
    const paletaPadrao = ["#10b981", "#3b82f6", "#8b5cf6", "#ef4444"];

    let anguloAtual = -0.5 * Math.PI; // Inicia no topo
    let corIdx = 0;

    for (const [chave, valor] of Object.entries(dados)) {
        if (valor <= 0) continue;

        const fatiaAngulo = (valor / total) * 2 * Math.PI;
        const cor = mapaCores[chave] || paletaPadrao[corIdx % paletaPadrao.length];
        corIdx++;

        // Desenho da fatia
        ctx.beginPath();
        ctx.moveTo(centroX, centroY);
        ctx.arc(centroX, centroY, raio, anguloAtual, anguloAtual + fatiaAngulo);
        ctx.closePath();
        ctx.fillStyle = cor;
        ctx.fill();

        // Linha divisória
        ctx.strokeStyle = "#ffffff";
        ctx.lineWidth = 2;
        ctx.stroke();

        anguloAtual += fatiaAngulo;

        // Adicionar item na legenda se o container existir (com percentuais ex: Concluído 60%)
        if (legenda) {
            const percentual = Math.round((valor / total) * 100);
            const itemLegenda = document.createElement("div");
            itemLegenda.style.display = "flex";
            itemLegenda.style.alignItems = "center";
            itemLegenda.style.justifyContent = "space-between";
            itemLegenda.style.gap = "8px";
            itemLegenda.style.fontSize = "13px";
            itemLegenda.innerHTML = `
                <div style="display:flex; align-items:center; gap:8px;">
                    <span style="display:inline-block; width:12px; height:12px; border-radius:3px; background:${cor}; flex-shrink:0;"></span>
                    <strong style="color: var(--ink);">${chave}:</strong>
                </div>
                <span style="color: var(--muted); font-weight:600;">${percentual}% (${valor})</span>
            `;
            legenda.appendChild(itemLegenda);
        }
    }

    // Efeito Donut no centro
    ctx.beginPath();
    ctx.arc(centroX, centroY, raio * 0.52, 0, 2 * Math.PI);
    ctx.fillStyle = "#ffffff";
    ctx.fill();
    ctx.strokeStyle = "#dce6df";
    ctx.lineWidth = 1;
    ctx.stroke();

    // Rótulo "TOTAL" e valor no centro do Donut
    ctx.fillStyle = "#64766e";
    ctx.font = "bold 10px 'DM Sans', sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText("TOTAL", centroX, centroY - 8);

    ctx.fillStyle = "#18342b";
    ctx.font = "bold 16px 'Space Grotesk', sans-serif";
    ctx.fillText(`${total}`, centroX, centroY + 10);
}

function normalizar(str) {
    return (str || "").toLowerCase().trim();
}

function comparaValores(str1, str2) {
    return normalizar(str1).includes(normalizar(str2));
}

function escapeHtml(text) {
    if (!text) return "";
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Inicia listagem ao carregar
Exibir();

// Reload automático a cada 10 segundos na view Web
setInterval(async () => {
    await Exibir();
}, 10000);

document.addEventListener("DOMContentLoaded", Exibir);