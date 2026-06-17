const API = "http://localhost:8080/api";
const STRONG_PASSWORD_MESSAGE =
    "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.";

function isStrongPassword(password) {
    return password.length >= 8
        && /[A-Z]/.test(password)
        && /[a-z]/.test(password)
        && /[0-9]/.test(password)
        && /[^A-Za-z0-9]/.test(password);
}

function register() {
    const password = document.getElementById("registerPassword").value;
    const result = document.getElementById("registerResult");

    if (!isStrongPassword(password)) {
        result.innerText = STRONG_PASSWORD_MESSAGE;
        return;
    }

    fetch(API + "/users/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            name: document.getElementById("registerName").value,
            email: document.getElementById("registerEmail").value,
            password: password
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.userId) {
            result.innerText = "Account created successfully. Redirecting to login...";

            setTimeout(() => {
                window.location.href = "login.html";
            }, 1000);
        } else {
            result.innerText = data.error || "Registration failed";
        }
    })
    .catch(() => {
        result.innerText = "Could not register. Please make sure the server is running.";
    });
}
