const ADMIN_API = "http://localhost:8080/api";

window.onload = function() {
    const userId = localStorage.getItem("userId");
    const userRole = localStorage.getItem("userRole");
    const userName = localStorage.getItem("userName") || "Admin";

    if (!userId) {
        window.location.href = "login.html";
        return;
    }

    if (userRole !== "ADMIN") {
        window.location.href = "index.html";
        return;
    }

    document.getElementById("adminStatus").innerText = "Logged in as " + userName + " (ADMIN)";
    loadAnalytics(userId);
};

function loadAnalytics(userId) {
    fetch(ADMIN_API + "/admin/analytics", {
        headers: {
            "X-User-Id": userId
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) {
            document.getElementById("analyticsResult").innerText = data.error;
            return;
        }

        renderSummary(data.summary || {});
        renderRoomUtilisation(data.roomUtilisation || []);
        renderBookingTrends(data.bookingTrends || []);
        renderUserActivity(data.userActivity || []);
        renderSecurityMetrics(data.securityMetrics || {});
    })
    .catch(() => {
        document.getElementById("analyticsResult").innerText =
            "Could not load analytics. Please make sure the server is running.";
    });
}

function renderSummary(summary) {
    document.getElementById("totalBookings").innerText = summary.totalBookings || 0;
    document.getElementById("confirmedBookings").innerText = summary.confirmedBookings || 0;
    document.getElementById("cancelledBookings").innerText = summary.cancelledBookings || 0;
    document.getElementById("totalRooms").innerText = summary.totalRooms || 0;
    document.getElementById("totalUsers").innerText = summary.totalUsers || 0;
}

function renderRoomUtilisation(rooms) {
    const list = document.getElementById("roomUtilisationList");
    list.innerHTML = "";

    if (rooms.length === 0) {
        list.appendChild(emptyReport("No rooms found."));
        return;
    }

    rooms.forEach(room => {
        list.appendChild(reportRow(
            room.name,
            room.bookingCount + " bookings",
            room.bookedHours + " booked hours | Capacity " + room.capacity
        ));
    });
}

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

function renderUserActivity(users) {
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

function renderSecurityMetrics(metrics) {
    document.getElementById("adminUsers").innerText = metrics.adminUsers || 0;
    document.getElementById("standardUsers").innerText = metrics.standardUsers || 0;
    document.getElementById("cancellationRate").innerText = (metrics.cancellationRate || 0) + "%";
    document.getElementById("loginEventTracking").innerText = metrics.loginEventTracking || "-";
}

function reportRow(title, value, detail) {
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

function logout() {
    localStorage.removeItem("userId");
    localStorage.removeItem("userName");
    localStorage.removeItem("userRole");
    window.location.href = "login.html";
}
