document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.querySelector('form');

    if (loginForm) {
        loginForm.addEventListener('submit', function(event) {
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // Validação básica no cliente
            if (!username || !password) {
                event.preventDefault();
                alert('Por favor, preencha todos os campos!');
            }
        });
    }
});