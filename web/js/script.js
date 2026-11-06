const url = "http://localhost:7070";

async function Cadastrar() {
    const nome = document.getElementById("nome").value;
    const descricao = document.getElementById("descricao").value;
    const categoria = document.getElementById("categoria").value;
    const status = document.getElementById("status").value;    
    const enviar = {
        nome,
        descricao,
        categoria,
        status
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

async function Deletar(id) {
    await fetch(`${url}/api/projetos/${id}`, {
        method: "DELETE"
    });

    Exibir();
}

async function Exibir() {
    const resposta = await fetch(`${url}/api/projetos`);
    const projetos = await resposta.json();
    let corpo = document.getElementById("projetos-body");

    corpo.replaceChildren();

    for (const projeto of projetos) {
        corpo.insertAdjacentHTML(
            "beforeend",
            `<tr>
                <td>${projeto.nome}</td>
                <td>${projeto.descricao}</td>
                <td>${projeto.categoria}</td>
                <td>${projeto.status}</td>
            </tr>`
        );
    }
}

Exibir();