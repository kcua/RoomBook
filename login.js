const API = "http://localhost:8080/api";

function login() {
    fetch(API + "/users/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.userId) {
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("userName", data.name);
            localStorage.setItem("userRole", data.role);

            document.getElementById("loginResult").innerText =
                "Login successful. Redirecting...";

            window.location.href = "index.html";
        } else {
            document.getElementById("loginResult").innerText =
                data.error || "Login failed";
        }
    })
    .catch(() => {
        document.getElementById("loginResult").innerText =
            "Could not login. Please make sure the server is running.";
    });
}
