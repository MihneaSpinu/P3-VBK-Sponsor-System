document.getElementById("loginForm").addEventListener("submit", event => {
    event.preventDefault();

    let username = document.getElementById("username").value;
    let password = document.getElementById("password").value;
    let rememberMe = document.getElementById("rememberMe").checked;
    let errorMessage = document.getElementById("errorMessage");

    // let hashedPassword = bcrypt.hashSync(password, 8);
    // console.log(hashedPassword);

    // Create a FormData object
    let formData = new FormData();

    // Append the data to the FormData object
    formData.append("username", username);
    formData.append("hashedPassword", password);  // assuming you're sending the raw password here
    formData.append("rememberMe", rememberMe);

    // Send the FormData using a POST request
    fetch("/login/confirm", {
        method: "POST",
        body: formData  // sending FormData in the body
    })
    .catch(() => {
        errorMessage.textContent = "Wrong username or password";
    });
});
