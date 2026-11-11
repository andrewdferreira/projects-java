const url = "http://localhost:7070";

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

    console.log(enviar);

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

async function CarregarSelecionado(id) {
    const resposta = await fetch(`${url}/api/projetos`);
    const projetos = await resposta.json();
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
                        <input type="text" id="nomeEditar" name="nome" placeholder="${projeto.nome}" required>
                    </label>
                    <label class="field">
                        <span>Descrição</span>
                        <input type="text" id="descricaoEditar" name="descricao" placeholder="${projeto.descricao}" required>
                    </label>
                    <label class="field">
                        <span>Categoria</span>
                        <select id="categoriaEditar" name="categoria" required>
                            <option value="web" ${projeto.categoria === "web" ? "selected" : ""}>Web</option>
                            <option value="mobile" ${projeto.categoria === "mobile" ? "selected" : ""}>Mobile</option>
                            <option value="desktop" ${projeto.categoria === "desktop" ? "selected" : ""}>Software</option>
                            <option value="outros" ${projeto.categoria === "outros" ? "selected" : ""}>Outro</option>
                        </select>
                    </label>
                    <label class="field">
                        <span>Status</span>
                        <select id="statusEditar" name="status" required>
                            <option value="concluido" ${projeto.status === "concluido" ? "selected" : ""}>Concluído</option>
                            <option value="em-andamento" ${projeto.status === "em-andamento" ? "selected" : ""}>Em Andamento</option>
                            <option value="Planejado" ${projeto.status === "Planejado" ? "selected" : ""}>Planejado</option>
                        </select>
                    </label>
                </div>
                <div class="form-actions">
                    <button class="button button-quiet" type="reset">Limpar formulário</button>
                    <div class="button-group">
                        <button class="button button-primary" type="button" onclick="EditarSelecionado(${projeto.id})">Editar projeto</button>
                        <button class="button button-danger" type="button" onclick="CancelarEdicao()">Cancelar </button>
                    </div>
                </div>
            </form>
        </section>`;

        let meuModal = new bootstrap.Modal(document.getElementById("modalEditar"));
        meuModal.show();
}

async function EditarSelecionado(id) {
    const nome = document.getElementById("nomeEditar").value = projeto.nome;
    const descricao = document.getElementById("descricaoEditar").value = projeto.descricao;
    const categoria = document.getElementById("categoriaEditar").value = projeto.categoria;
    const status = document.getElementById("statusEditar").value = projeto.status;

    const enviar = {
    nome,
    descricao,
    categoria,
    status
  };
  console.log(enviar);

  await fetch(`${url}/api/projeto/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(enviar),
  });

  Exibir();

  let modal = bootstrap.Modal.getInstance(document.getElementById("modalEditar"))
  modal.hide();
}

async function Exibir() {
    const resposta = await fetch(`${url}/api/projetos`);
    const projetos = await resposta.json();
    let corpo = document.getElementById("projetos-tbody");
    let selecionar = document.getElementById("projetos-select");

    corpo.replaceChildren();

    for (const projeto of projetos) {
        corpo.insertAdjacentHTML(
            "beforeend",
            `<tr>
                <th>${projeto.id}</th>
                <th>${projeto.nome}</th>
                <th>${projeto.descricao}</th>
                <th>${projeto.categoria}</th>
                <th>${projeto.status}</th>
                <th>
                    <input type="checkbox" id="projeto-${projeto.id}" name="projeto-${projeto.id}" class="projetos-select">
                    <label for="projeto-${projeto.id}"></label>
                </th>
            </tr>`
        );
    }
}

Exibir();