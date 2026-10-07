# ☕ Sistema de Projetos

Sistema de gerenciamento de projetos desenvolvido em **Java** ao longo das aulas da faculdade, como parte do aprendizado prático em Análise e Desenvolvimento de Sistemas.

O projeto reúne conceitos de programação orientada a objetos, organização em camadas, persistência de dados em arquivos, interfaces gráficas e desenvolvimento de APIs REST.

## ✨ Funcionalidades

* **Cadastro de projetos:** registro de nome, descrição, categoria e status.
* **Consulta de projetos:** listagem e busca por identificador.
* **Edição de projetos:** atualização das informações cadastradas.
* **Exclusão de projetos:** remoção de registros.
* **Filtros e pesquisas:** busca por nome, categoria e status.
* **Persistência de dados:** armazenamento e carregamento dos projetos em arquivo CSV.
* **Interface gráfica:** tela desenvolvida com Java Swing para cadastrar e visualizar projetos.
* **Interface via terminal:** menu interativo para executar as operações de gerenciamento.
* **API REST:** endpoints HTTP para consultar, cadastrar, atualizar e excluir projetos.
* **Validação de dados:** verificação do preenchimento obrigatório do nome do projeto.

## 🛠️ Tecnologias utilizadas

* **Java 21** — linguagem de programação.
* **Maven** — gerenciamento de dependências e configuração do projeto.
* **Javalin** — desenvolvimento da API REST.
* **Jackson** — conversão de objetos Java para JSON e vice-versa.
* **SLF4J** — registro de logs.
* **Java Swing** — construção da interface gráfica.
* **CSV** — armazenamento dos dados.
* **Git e GitHub** — versionamento e hospedagem do código.

## 🏗️ Organização do projeto

O código está organizado em pacotes com responsabilidades distintas:

| Pacote    | Responsabilidade                                 |
| --------- | ------------------------------------------------ |
| `api`     | Configuração das rotas HTTP e respostas da API.  |
| `dao`     | Leitura e gravação dos dados em CSV.             |
| `model`   | Representação dos projetos e seus atributos.     |
| `service` | Regras de negócio e operações sobre os projetos. |
| `view`    | Interface gráfica desenvolvida com Swing.        |

A classe `Main` inicializa a API, abre a interface gráfica e disponibiliza o menu de operações no terminal.

## 🌐 API REST

A API é executada localmente na porta `7070`.

**URL base:** `http://localhost:7070`

| Método | Endpoint             | Descrição                       |
| ------ | -------------------- | ------------------------------- |
| GET    | `/`                  | Verifica a rota inicial da API. |
| GET    | `/api/projetos`      | Lista os projetos cadastrados.  |
| GET    | `/api/projetos/{id}` | Busca um projeto pelo ID.       |
| POST   | `/api/projetos`      | Cadastra um novo projeto.       |
| PUT    | `/api/projetos/{id}` | Atualiza um projeto existente.  |
| DELETE | `/api/projetos/{id}` | Exclui um projeto pelo ID.      |

### Exemplo de requisição

Para cadastrar um projeto, envie uma requisição `POST` para `/api/projetos` com um corpo JSON como este:

```json
{
  "nome": "Meu projeto",
  "descricao": "Descrição do projeto",
  "categoria": "Software",
  "status": "Planejado"
}
```

As requisições e respostas da API utilizam JSON para representar os dados dos projetos.

## 🚀 Como executar

### Pré-requisitos

* JDK 21 ou compatível.
* Uma IDE compatível com Java, como IntelliJ IDEA.
* Maven, integrado à IDE ou instalado no sistema.

### Passos

1. Clone o repositório:

   ```bash
   git clone https://github.com/andrewdferreira/projects-java.git
   ```

2. Acesse a pasta do projeto:

   ```bash
   cd projects-java
   ```

3. Abra o projeto na IDE e importe as dependências pelo arquivo `pom.xml`.

4. Execute a classe `Main.java`.

A aplicação inicializa a API e abre a interface gráfica. O menu de gerenciamento também fica disponível no terminal.

## 📚 Objetivo acadêmico

Este projeto foi desenvolvido ao longo das aulas da faculdade, com foco na aplicação prática dos conceitos estudados e na evolução contínua das habilidades de desenvolvimento em Java.

Durante sua construção, são explorados conceitos como:

* Programação orientada a objetos.
* Separação de responsabilidades.
* Estruturas de dados e coleções.
* Manipulação de arquivos.
* Desenvolvimento de interfaces gráficas.
* Criação e consumo de endpoints HTTP.
* Serialização e desserialização de JSON.
* Versionamento de código com Git.

## 🔄 Evolução do projeto

O sistema faz parte do meu processo de aprendizado e poderá receber melhorias conforme o avanço dos estudos e a implementação de novas funcionalidades.

## 👨‍💻 Autor

**Andrew Ferreira**

Estudante de Análise e Desenvolvimento de Sistemas e Técnico em Redes de Computadores.

* GitHub: [@andrewdferreira](https://github.com/andrewdferreira)
* Repositório: [projects-java](https://github.com/andrewdferreira/projects-java)

---

*Projeto acadêmico desenvolvido para fins educacionais.*
