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
                            <option value="Mobile" ${projeto.categoria === "Mobile" ? "selected" : ""}>Mobile</option>
                            <option value="Desktop" ${projeto.categoria === "Desktop" ? "selected" : ""}>Software</option>
                            <option value="Outros" ${projeto.categoria === "Outros" ? "selected" : ""}>Outro</option>
                        </select>
                    </label>
                    <label class="field">
                        <span>Status</span>
                        <select id="statusEditar" name="status" required>
                            <option value="Concluído" ${projeto.status === "Concluído" ? "selected" : ""}>Concluído</option>
                            <option value="Em andamento" ${projeto.status === "Em andamento" ? "selected" : ""}>Em Andamento</option>
                            <option value="Planejado" ${projeto.status === "Planejado" ? "selected" : ""}>Planejado</option>
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

    // Contadores
    const categorias = {};
    const statusMap = {};

    let concluidos = 0;
    let andamento = 0;
    let planejados = 0;

    projetos.forEach(p => {
        // Normalização de categoria
        let cat = normalizar(p.categoria);
        if (cat.includes("web")) cat = "Web";
        else if (cat.includes("soft") || cat.includes("desk")) cat = "Software";
        else if (cat.includes("mobi")) cat = "Mobile";
        else cat = "Outros";

        categorias[cat] = (categorias[cat] || 0) + 1;

        // Normalização de status
        let st = normalizar(p.status);
        if (st.includes("conclu")) {
            st = "Concluído";
            concluidos++;
        } else if (st.includes("andamento") || st.includes("desenvolvimento")) {
            st = "Em andamento";
            andamento++;
        } else {
            st = "Planejado";
            planejados++;
        }

        statusMap[st] = (statusMap[st] || 0) + 1;
    });

    // Atualizar KPIs
    document.getElementById("kpiTotal").textContent = projetos.length;
    document.getElementById("kpiConcluidos").textContent = `${concluidos} (${projetos.length ? Math.round(concluidos * 100 / projetos.length) : 0}%)`;
    document.getElementById("kpiAndamento").textContent = andamento;
    document.getElementById("kpiPlanejados").textContent = planejados;

    // Cores padronizadas para categorias
    const coresCat = {
        "Web": "#2563eb",
        "Software": "#176b51",
        "Mobile": "#7c3aed",
        "Outros": "#f59e0b"
    };

    const coresSt = {
        "Concluído": "#10b981",
        "Em andamento": "#3b82f6",
        "Planejado": "#8b5cf6"
    };

    DesenharPizza("canvasCategorias", "legendaCategorias", categorias, coresCat);
    DesenharPizza("canvasStatus", "legendaStatus", statusMap, coresSt);
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
    const paletaPadrao = ["#2563eb", "#176b51", "#7c3aed", "#f59e0b", "#10b981", "#ec4899", "#06b6d4"];

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

        // Adicionar item na legenda se o container existir
        if (legenda) {
            const percentual = ((valor / total) * 100).toFixed(1);
            const itemLegenda = document.createElement("div");
            itemLegenda.style.display = "flex";
            itemLegenda.style.alignItems = "center";
            itemLegenda.style.gap = "8px";
            itemLegenda.style.fontSize = "13px";
            itemLegenda.innerHTML = `
                <span style="display:inline-block; width:12px; height:12px; border-radius:3px; background:${cor}; flex-shrink:0;"></span>
                <strong style="color: var(--ink);">${chave}:</strong> <span style="color: var(--muted);">${valor} (${percentual}%)</span>
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