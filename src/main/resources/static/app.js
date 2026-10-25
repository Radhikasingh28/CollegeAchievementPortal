const $ = id => document.getElementById(id);
let students = [], toastTimer;
function esc(v) { return String(v ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c])); }
function initials(n) { return (n || "?").split(/\s+/).slice(0, 2).map(x => x[0]).join("").toUpperCase(); }
function toast(msg) { $("toast").textContent = msg; $("toast").classList.remove("hidden"); clearTimeout(toastTimer); toastTimer = setTimeout(() => $("toast").classList.add("hidden"), 3000); }
async function api(url, opt = {}) { let r = await fetch(url, { headers: { "Content-Type": "application/json" }, ...opt }); let b = await r.json().catch(() => ({})); if (!r.ok) throw Error(b.message || b.error || `Request failed (${r.status})`); return b; }
function showView(v) { document.querySelectorAll(".view").forEach(x => x.classList.add("hidden")); $(`${v}-view`).classList.remove("hidden"); document.querySelectorAll(".nav").forEach(x => x.classList.toggle("active", x.dataset.view === v)); $("page-title").textContent = { overview: "Overview", achievements: "Achievements", leaderboard: "Top 100 Leaderboard" }[v]; if (v === "achievements") loadAchievements(); if (v === "leaderboard") loadLeaderboard(); }
document.querySelectorAll(".nav").forEach(x => x.addEventListener("click", () => showView(x.dataset.view)));
document.querySelectorAll("[data-goto]").forEach(x => x.addEventListener("click", () => showView(x.dataset.goto)));
function person(name, roll) { return `<div class="person"><div class="person-avatar">${esc(initials(name))}</div><div><strong>${esc(name)}</strong><small>${esc(roll)}</small></div></div>`; }
function row(s, i, compact = false) { return `<tr><td><span class="rank">#${i + 1}</span></td><td>${person(s.fullName, s.rollNumber)}</td>${compact ? "" : `<td>${esc(s.rollNumber)}</td>`}<td>${esc(s.department)}</td><td>${esc(s.verifiedAchievements)}</td><td><span class="score">${esc(s.score)} pts</span></td></tr>`; }
async function loadOverview() { let [s, b, a] = await Promise.all([api("/api/stats"), api("/api/leaderboard?limit=5"), api("/api/achievements")]); $("stat-students").textContent = s.students; $("stat-achievements").textContent = s.totalAchievements; $("stat-verified").textContent = s.verifiedAchievements; $("stat-pending").textContent = s.pendingAchievements; $("preview-body").innerHTML = b.map(row).join("") || `<tr><td colspan="5">No students yet</td></tr>`; $("recent-list").innerHTML = a.slice(0, 3).map(card).join("") || `<div class="empty">No achievements yet.</div>`; }
function card(a) { let pending = a.status === "PENDING"; return `<article class="achievement-card"><div><p class="achievement-title">${esc(a.title)}</p><div class="achievement-meta">${esc(a.studentName)} · ${esc(a.department)} · ${esc(a.category)} · ${esc(a.achievementLevel)}<br>${esc(a.description || "No description")}${a.proofUrl ? ` · <a href="${esc(a.proofUrl)}" target="_blank" rel="noopener">View proof ↗</a>` : ""}</div></div><div class="achievement-right"><span class="pill ${esc(a.status)}">${esc(a.status)}</span><span class="score">${esc(a.points)} pts</span>${pending ? `<div class="review-actions"><button class="approve" onclick="review(${a.id},'VERIFIED','${esc(a.category)}','${esc(a.achievementLevel)}')">Approve</button><button class="reject" onclick="review(${a.id},'REJECTED','${esc(a.category)}','${esc(a.achievementLevel)}')">Reject</button></div>` : ""}</div></article>`; }
async function loadAchievements() { try { let p = new URLSearchParams({ status: $("status-filter").value, search: $("search").value.trim() }); let a = await api(`/api/achievements?${p}`); $("achievement-list").innerHTML = a.map(card).join("") || `<div class="empty">No matching achievements.</div>`; } catch (e) { $("achievement-list").innerHTML = `<div class="error">${esc(e.message)}</div>`; } }
async function loadLeaderboard() { try { let a = await api("/api/leaderboard?limit=100"); $("leaderboard-body").innerHTML = a.map((s, i) => row(s, i, false)).join("") || `<tr><td colspan="6">No students found</td></tr>`; } catch (e) { $("leaderboard-body").innerHTML = `<tr><td colspan="6">${esc(e.message)}</td></tr>`; } }
async function review(id, status, category, achievementLevel) { try { await api(`/api/achievements/${id}/review`, { method: "PATCH", body: JSON.stringify({ status, category, achievementLevel }) }); toast(status === "VERIFIED" ? "Achievement verified; points awarded." : "Achievement rejected."); await loadOverview(); await loadAchievements(); } catch (e) { toast(e.message); } }
window.review = review;
function openModal() { $("modal").classList.remove("hidden"); $("student-id").innerHTML = students.map(s => `<option value="${s.id}">${esc(s.fullName)} · ${esc(s.rollNumber)}</option>`).join(""); }
function closeModal() { $("modal").classList.add("hidden"); }
$("open-add").addEventListener("click", openModal); $("close-modal").addEventListener("click", closeModal); $("cancel-modal").addEventListener("click", closeModal); $("modal").addEventListener("click", e => { if (e.target === $("modal")) closeModal(); });
$("search").addEventListener("input", () => { clearTimeout(window.searchDelay); window.searchDelay = setTimeout(loadAchievements, 250); }); $("status-filter").addEventListener("change", loadAchievements);
$("achievement-form").addEventListener("submit", async e => { e.preventDefault(); let d = { studentId: Number($("student-id").value), title: $("title").value.trim(), category: $("category").value, achievementLevel: $("level").value, description: $("description").value.trim(), proofUrl: $("proof").value.trim() }; try { await api("/api/achievements", { method: "POST", body: JSON.stringify(d) }); closeModal(); e.target.reset(); toast("Achievement submitted for review."); await loadOverview(); if (!$("achievements-view").classList.contains("hidden")) await loadAchievements(); } catch (err) { toast(err.message); } });
async function init() { try { students = await api("/api/students"); await loadOverview(); } catch (e) { document.querySelector(".main").insertAdjacentHTML("afterbegin", `<div class="error"><b>Could not connect to database.</b><br>${esc(e.message)}<br>Check MySQL password in application.properties, then restart.</div>`); } }
init();


const loginForm = document.getElementById("login-form");

loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const message = document.getElementById("login-message");
    const username = document.getElementById("login-username").value.trim();
    const password = document.getElementById("login-password").value;
    const role = document.getElementById("login-role").value;

    if (!username || !password || !role) {
        message.textContent = "Please fill in all fields.";
        return;
    }

    message.textContent = "Logging in...";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                username: username,
                password: password,
                role: role
            })
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || "Login failed.");
        }

        message.textContent = "Login successful!";

        document.getElementById("login-screen").classList.add("hidden");
        document.getElementById("dashboard").classList.remove("hidden");

        await loadOverview();

    } catch (error) {
        console.error("Login error:", error);
        message.textContent = error.message;
    }
});
