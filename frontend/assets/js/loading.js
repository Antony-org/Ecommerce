function loadingHeaderContent(){
    const token = localStorage.getItem("token");
    if(token === null){
        document.getElementById("login").innerHTML = '<a class="nav-link" href="/web/auth/login.html">Login</a>';

    }
    else{
        document.getElementById("login").innerHTML = '<a class="nav-link" onclick="invalidateToken()">Logout</a>\n';
        document.getElementById("profileSection").innerHTML = `
                        <br>
                        <li class="nav-item">
                            <button class="search">
                                <a href="/web/profile.html" class="cart"><span class="lnr fa-regular fa-user" style="color: white" id="search"></span></a>
                            </button>
                        </li>`
    }
}

function invalidateToken(){
    localStorage.removeItem("token");
    location.href = "/web/index.html";
}