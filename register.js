const API = "http://localhost:8080/api";

function register() {
    fetch(API + "/users/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            name: document.getElementById("registerName").value,
            email: document.getElementById("registerEmail").value,
            password: document.getElementById("registerPassword").value
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.userId) {
            document.getElementById("registerResult").innerText =
                "Account created successfully. Redirecting to login...";

            setTimeout(() => {
                window.location.href = "login.html";
            }, 1000);
        } else {
            document.getElementById("registerResult").innerText =
                data.error || "Registration failed";
        }
    })
    .catch(() => {
        document.getElementById("registerResult").innerText =
            "Could not register. Please make sure the server is running.";
    });
}
