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
    const checkboxes = document.querySelectorAll('input[name^="projeto-${projeto.id}"]:checked');
    const ids = Array.from(checkboxes).map(checkbox => checkbox.id.split('-')[2]);

    for (const id of ids) {
        await fetch(`${url}/api/projetos/${id}`, {
            method: "DELETE"
        });
    }

    Exibir();
}

async function Exibir() {
    const resposta = await fetch(`${url}/api/projetos`);
    const projetos = await resposta.json();
    console.log(projetos);
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