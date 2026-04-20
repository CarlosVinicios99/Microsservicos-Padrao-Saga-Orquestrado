 <h1>Arquitetura de Microsserviços - Padrão Saga Orquestrado</h1>
<p>
  Neste projeto é implementado o padrão Saga Orquestrado, um padrão de arquitetura de microsserviços que tem como objetivo garantir o sucesso em um fluxo de transações e 
  em caso de falhas, garantir o rollback em todo fluxo, mantendo a consistência dos dados e das funcionalides
</p>
<h2>Características</h2>
<p>
  O padrão Saga Orquestrado mantém um serviço orquestrador, responsável por orquestrar a execução da Saga (Fluxo), somente o orquestrador possuí conhecimento sobre a 
  ordem dos serviços que devem ser executados, de modo que os microsserviços ficam totalmente desacoplados entre si, sem possuir informações um dos outros e sobre 
  a ordem de execução dos demais serviços. Toda lógica envolvendo a orquestração dos serviços fica centralizada e isolada no serviço orquestrador.
</p>

<h2>Estudo de Caso</h2>
<p>
  Para a implementação do padrão foi utilizado um estudo de caso que simula um fluxo da criação de pedidos, passando por validação dos produtos do pedido, pagamento e baixa em inventário.
</p>
<img src="https://github.com/CarlosVinicios99/Microsservicos-Padrao-Saga-Orquestrado/blob/main/estudo-de-caso.png?raw=true">
<hr>

<h2>Tópicos do Kafka - Visualização Redpanda Console</h2>
<img src="https://github.com/CarlosVinicios99/Microsservicos-Padrao-Saga-Orquestrado/blob/main/eventos-redpanda-console.jpeg?raw=true">
<hr>

<h2>Tecnologias e Ferramentas utilizadas</h2>
<img src="https://github.com/CarlosVinicios99/Microsservicos-Padrao-Saga-Orquestrado/blob/main/tecnologias-utilizadas.png?raw=true">
<hr>
