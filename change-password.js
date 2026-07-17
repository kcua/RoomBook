const ROOMBOOK_API = "http://localhost:8080/api";
const STRONG_PASSWORD_MESSAGE =
    "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character."; //not strong enough password message

let loggedInUserId = localStorage.getItem("userId"); //gets userid

window.onload = function() { 
    if (!loggedInUserId) {
        window.location.href = "login.html";
        return;
    }
 

    //gets userdetails from local storage
    const userName = localStorage.getItem("userName") || "User";
    const userRole = localStorage.getItem("userRole");
    document.getElementById("userStatus").innerText = //displays username and role
        "Logged in as " + userName + (userRole ? " (" + userRole + ")" : "");
};

function isStrongPassword(password) { //chekcs if the password meets the requiremnts
    return password.length >= 8
        && /[A-Z]/.test(password)
        && /[a-z]/.test(password)
        && /[0-9]/.test(password)
        && /[^A-Za-z0-9]/.test(password);
}

function changePassword() { //sends a request to change password to the API
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const result = document.getElementById("passwordResult");

    if (!isStrongPassword(newPassword)) { //checks the password first before sending it to server
        result.innerText = STRONG_PASSWORD_MESSAGE;
        return;
    }

    fetch(ROOMBOOK_API + "/users/change-password", { //sends password change details to backend
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            userId: parseInt(loggedInUserId),
            currentPassword: currentPassword,
            newPassword: newPassword
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) { //if there's an error in backend, show error message.
            result.innerText = data.error;
        } else {
            result.innerText = data.message || "Password changed successfully."; //shows successful message 
            document.getElementById("currentPassword").value = "";
            document.getElementById("newPassword").value = "";
        }
    })
    .catch(() => {
        result.innerText = "Could not change password. Please try again.";
    });
}

//logs user out and redirects to login page
function logout() { 
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userRole");
    window.location.href = "login.html";
}
