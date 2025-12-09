document.getElementById("loginForm").addEventListener("submit", event => {
    event.preventDefault();

    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let rememberMe = document.getElementById("rememberMe").checked;
    let errorMessage = document.getElementById("errorMessage");



    let formData = new FormData();

    formData.append("username", username);
    formData.append("password", password);
    formData.append("rememberMe", rememberMe);

    fetch("/login/confirm", {
        method: "POST",
        body: formData
    })
    .then(() => {
        //window.location.href = "/homepage";
    })
    .catch(() => {
        errorMessage.textContent = "Wrong username or password";
    });
});
