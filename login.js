const API = "http://localhost:8080/api";

// login function runs when user tries logging in
function login() {
    // Send the email and password to the backend login endpoint.
    fetch(API + "/users/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            // the code below gets the user's input from the login form
            email: document.getElementById("loginEmail").value,
            password: document.getElementById("loginPassword").value
        })
    })
    // convert the server response from JSON into a JavaScript object.
    .then(res => res.json())
    .then(data => {
        // If the backend returns a userId, the login was successful.
        if (data.userId) {
            // Store the logged-in user's details so other pages can use them.
            const fallbackToken = data.sessionToken || (crypto && crypto.randomUUID ? crypto.randomUUID() : "session-" + Date.now());
            localStorage.setItem("userId", data.userId);
            localStorage.setItem("userName", data.name);
            localStorage.setItem("userRole", data.role);
            localStorage.setItem("sessionToken", fallbackToken);

            // Show a success message on the login page.
            document.getElementById("loginResult").innerText =
                "Login successful. Redirecting...";

            //  if login is successful, it moves to the main page which is index.html
            window.location.href = "index.html";
        } else {
            // If login failed, show the error from the backend if available.
            document.getElementById("loginResult").innerText =
                data.error || "Login failed";
        }
    })
    .catch(() => {
        // This runs if the request fails, for example if the backend is not running.
        document.getElementById("loginResult").innerText =
            "Could not login. Please make sure the server is running.";
    });
}
