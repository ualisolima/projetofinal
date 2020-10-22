# projetofinal
 
# Mensagens

## Descrição do Projeto
Esse aplicativo simula o whatsapp. Uma aplicação android que permite os usuários enviar mensagens, compartilhar imagens usando a camera ou galeria e compartilhar a localização.
O usuário se registra com o número de telefone e encontra os amigos através da lista de contatos.

## Tools and APIs usadas para esse projeto

* Android Studio
* Firebase Invite
* Firebase Authentication
* Firebase Storage
* Firebase Realtime Database

## Funcionalidades

 * O mensagens usa a Autenticação de número de telefone do Firebase, que verifica o número de telefone de um usuário enviando um código para ele. O usuário é então autenticado para usar o aplicativo. Em seguida, o usuário é solicitado a fornecer um nome e uma foto de perfil. A foto do perfil é armazenada no ** Firebase Storage **.

 - Obs.: Só é permitido numeros prefedinidos nos testes do firebase, use um desses números para criar uma conta:
 - +1 970 691 6165
 - +1 970 691 7233
 - +55 11 11111-1111
 - +55 22 22222-2222
 - +55 33 33333-3333

 * para todos esses números, o código de confirmacao é 123456.

 * O aplicativo usa o ** Firebase Realtime Database ** para mensagens de texto e compartilhamento de localização. Desta forma, o usuário recebe a mensagem instantaneamente.

 * O aplicativo usa ** Firebase Realtime Database ** e ** Firebase Storage ** para compartilhamento de imagens.

 * O aplicativo também usa o ** Firebase Invites ** para dar ao usuário a facilidade de convidar outras pessoas para usar este aplicativo por e-mail ou mensagem de texto.

 * O usuário também pode alterar seu nome, foto ou status. O status funciona da mesma maneira que no WhatsApp.
