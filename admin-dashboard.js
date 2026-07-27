const ADMIN_API = "http://localhost:8080/api";
const STRONG_PASSWORD_MESSAGE =
    "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character.";

let currentAdminUserId = null;

window.onload = function() {
    //Get saved user details from localstorage
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");
    const userName = localStorage.getItem("userName") || "Admin";
    const sessionToken = localStorage.getItem("sessionToken");

    if (!userId) { //if no logged-in user, it will send them back to login page.
        window.location.href = "login.html";
        return;
    }

    if (!sessionToken) {
        localStorage.setItem("sessionToken", "session-" + Date.now());
    }

    if (userRole !== "ADMIN") { // if user is not admin - send back to home page
        window.location.href = "index.html";
        return;
    }

    document.getElementById("adminStatus").innerText = "Logged in as " + userName + " (ADMIN)"; //display admin name on page
    currentAdminUserId = userId;
    setupUserManagement();
    loadUsers(userId);
    loadAnalytics(userId);
};

function setupUserManagement() {
    document.getElementById("createUserForm").addEventListener("submit", function(event) {
        event.preventDefault();
        createUser();
    });
}

function loadAnalytics(userId) { //this function requests analytics data from backend
    fetch(ADMIN_API + "/admin/analytics", {
        headers: {
            "X-User-Id": userId //checks permission by sending userID to the backend
        }
    })
    .then(res => res.json()) //converts backend response to JSOn
    .then(data => { //use the returned data to update the page
        if (data.error) { //if backends returns error, display on page
            document.getElementById("analyticsResult").innerText = data.error;
            return;
        }

        //displays each section of the analytics report
        renderSummary(data.summary || {});
        renderRoomUtilisation(data.roomUtilisation || []);
        renderBookingTrends(data.bookingTrends || []);
        renderUserActivity(data.userActivity || []);
        renderSecurityMetrics(data.securityMetrics || {});
    })
    .catch(() => { //error message if the server cannot be reached
        document.getElementById("analyticsResult").innerText =
            "Could not load analytics. Please make sure the server is running.";
    });
}

function renderSummary(summary) { //this function displays the summary statistics
    document.getElementById("totalBookings").innerText = summary.totalBookings || 0; 
    document.getElementById("confirmedBookings").innerText = summary.confirmedBookings || 0; 
    document.getElementById("cancelledBookings").innerText = summary.cancelledBookings || 0;
    document.getElementById("totalRooms").innerText = summary.totalRooms || 0;
    document.getElementById("totalUsers").innerText = summary.totalUsers || 0;
}

function renderRoomUtilisation(rooms) { //this function displays room utilisation data
    const list = document.getElementById("roomUtilisationList"); 
    list.innerHTML = ""; //clear any old data before adding new data

    if (rooms.length === 0) {
        list.appendChild(emptyReport("No rooms found."));
        return;
    }

    rooms.forEach(room => {  // Loop through each room and add it to the report
        list.appendChild(reportRow(
            room.name,
            room.bookingCount + " bookings",
            room.bookedHours + " booked hours | Capacity " + room.capacity
        ));
    });
}


//this function displays booking by trends by date
function renderBookingTrends(trends) {
    const list = document.getElementById("bookingTrendsList");
    list.innerHTML = "";

    if (trends.length === 0) {
        list.appendChild(emptyReport("No booking trends yet."));
        return;
    }

    trends.forEach(day => {
        list.appendChild(reportRow(
            day.date,
            day.bookingCount + " bookings",
            "Daily reservation count"
        ));
    });
}

function renderUserActivity(users) { // This function displays user activity data
    const list = document.getElementById("userActivityList");
    list.innerHTML = "";

    if (users.length === 0) {
        list.appendChild(emptyReport("No users found."));
        return;
    }

    users.forEach(user => {
        list.appendChild(reportRow(
            user.name,
            user.bookingCount + " bookings",
            user.activeBookings + " active | " + user.cancelledBookings + " cancelled | " + user.email
        ));
    });
}

function renderSecurityMetrics(metrics) { // This function displays security-related metrics
    document.getElementById("adminUsers").innerText = metrics.adminUsers || 0;
    document.getElementById("standardUsers").innerText = metrics.standardUsers || 0;
    document.getElementById("cancellationRate").innerText = (metrics.cancellationRate || 0) + "%";
    document.getElementById("loginEventTracking").innerText = metrics.loginEventTracking || "-";
}

function loadUsers(userId) {
    fetch(ADMIN_API + "/users", {
        headers: {
            "X-User-Id": userId
        }
    })
    .then(handleApiResponse)
    .then(users => {
        renderUsers(users || []);
    })
    .catch(error => {
        showUserManagementMessage(error.message || "Could not load users.");
    });
}

function createUser() {
    const name = document.getElementById("newUserName").value.trim();
    const email = document.getElementById("newUserEmail").value.trim();
    const password = document.getElementById("newUserPassword").value;
    const role = document.getElementById("newUserRole").value;

    if (!isStrongPassword(password)) {
        showUserManagementMessage(STRONG_PASSWORD_MESSAGE);
        return;
    }

    if (!confirm("Are you sure you want to create this user?")) {
        return;
    }

    fetch(ADMIN_API + "/users", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-User-Id": currentAdminUserId
        },
        body: JSON.stringify({ name, email, password, role })
    })
    .then(handleApiResponse)
    .then(() => {
        document.getElementById("createUserForm").reset();
        showUserManagementMessage("User created successfully.");
        refreshAdminData();
    })
    .catch(error => {
        showUserManagementMessage(error.message || "Could not create user.");
    });
}

function updateUserRole(userId, role, previousRole, roleSelect) {
    if (!confirm("Are you sure you want to change this user's role?")) {
        roleSelect.value = previousRole;
        return;
    }

    fetch(ADMIN_API + "/users/" + userId + "/role", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "X-User-Id": currentAdminUserId
        },
        body: JSON.stringify({ role })
    })
    .then(handleApiResponse)
    .then(() => {
        showUserManagementMessage("User role updated.");
        refreshAdminData();
    })
    .catch(error => {
        showUserManagementMessage(error.message || "Could not update user role.");
        roleSelect.value = previousRole;
        loadUsers(currentAdminUserId);
    });
}

function deleteUser(userId, name) {
    if (!confirm("Delete " + name + "? This will also remove their reservations.")) {
        return;
    }

    fetch(ADMIN_API + "/users/" + userId, {
        method: "DELETE",
        headers: {
            "X-User-Id": currentAdminUserId
        }
    })
    .then(handleApiResponse)
    .then(() => {
        showUserManagementMessage("User deleted successfully.");
        refreshAdminData();
    })
    .catch(error => {
        showUserManagementMessage(error.message || "Could not delete user.");
    });
}

function renderUsers(users) {
    const list = document.getElementById("adminUserList");
    list.innerHTML = "";

    if (users.length === 0) {
        list.appendChild(emptyReport("No users found."));
        return;
    }

    users.forEach(user => {
        const row = document.createElement("div");
        row.className = "user-row";

        const details = document.createElement("div");
        details.className = "user-row-main";

        const name = document.createElement("strong");
        name.textContent = user.name;

        const email = document.createElement("span");
        email.textContent = user.email;

        details.appendChild(name);
        details.appendChild(email);

        const controls = document.createElement("div");
        controls.className = "user-row-actions";

        const roleSelect = document.createElement("select");
        roleSelect.value = user.role;
        ["USER", "ADMIN"].forEach(role => {
            const option = document.createElement("option");
            option.value = role;
            option.textContent = role === "ADMIN" ? "Admin" : "User";
            roleSelect.appendChild(option);
        });
        roleSelect.value = user.role;
        roleSelect.addEventListener("change", function() {
            updateUserRole(user.userId, roleSelect.value, user.role, roleSelect);
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "danger";
        deleteButton.textContent = "Delete";
        deleteButton.disabled = String(user.userId) === String(currentAdminUserId);
        deleteButton.addEventListener("click", function() {
            deleteUser(user.userId, user.name);
        });

        controls.appendChild(roleSelect);
        controls.appendChild(deleteButton);
        row.appendChild(details);
        row.appendChild(controls);
        list.appendChild(row);
    });
}

function refreshAdminData() {
    loadUsers(currentAdminUserId);
    loadAnalytics(currentAdminUserId);
}

function showUserManagementMessage(message) {
    document.getElementById("userManagementResult").innerText = message;
}

function handleApiResponse(res) {
    return res.json().then(data => {
        if (!res.ok || data.error) {
            throw new Error(data.error || "Request failed.");
        }

        return data;
    });
}

function isStrongPassword(password) {
    return password.length >= 8
        && /[A-Z]/.test(password)
        && /[a-z]/.test(password)
        && /[0-9]/.test(password)
        && /[^A-Za-z0-9]/.test(password);
}

function reportRow(title, value, detail) { // This function creates one row for a report section
    const row = document.createElement("div");
    row.className = "report-row";

    const main = document.createElement("div");
    main.className = "report-main";

    const titleEl = document.createElement("strong");
    titleEl.textContent = title;

    const detailEl = document.createElement("span");
    detailEl.textContent = detail;

    const valueEl = document.createElement("div");
    valueEl.className = "report-value";
    valueEl.textContent = value;

    main.appendChild(titleEl);
    main.appendChild(detailEl);
    row.appendChild(main);
    row.appendChild(valueEl);

    return row;
}

function emptyReport(message) {
    const empty = document.createElement("div");
    empty.className = "empty-state";
    empty.textContent = message;
    return empty;
}

// This function logs the user out
function logout() {

    // Remove saved login details from localStorage
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userRole");
    localStorage.removeItem("sessionToken");
    window.location.href = "login.html";
}
