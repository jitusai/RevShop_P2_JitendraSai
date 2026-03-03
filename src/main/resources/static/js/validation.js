document.addEventListener('DOMContentLoaded', function() {
    // ── THEME INITIALIZATION ────────────────────────
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.body.setAttribute('data-theme', savedTheme);
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.textContent = savedTheme === 'dark' ? '☀️' : '🌙';
        themeToggle.addEventListener('click', () => {
            const currentTheme = document.body.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            document.body.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            themeToggle.textContent = newTheme === 'dark' ? '☀️' : '🌙';
        });
    }

    // ── FORM VALIDATION ─────────────────────────────

    // 1. Quantity Validation (Product Detail Page)
    const qtyInput = document.querySelector('.qty-input');
    const addToCartForm = document.querySelector('form[action="/cart/add"]');
    if (addToCartForm && qtyInput) {
        addToCartForm.addEventListener('submit', function(e) {
            const qty = parseInt(qtyInput.value);
            const max = parseInt(qtyInput.getAttribute('th:max') || 99); // Fallback
            if (qty <= 0) {
                alert('Quantity must be at least 1.');
                e.preventDefault();
            } else if (qty > max) {
                alert(`Only ${max} items available in stock.`);
                e.preventDefault();
            }
        });
    }

    // 2. Review Form Validation
    const reviewForm = document.querySelector('form[enctype="multipart/form-data"]');
    if (reviewForm) {
        reviewForm.addEventListener('submit', function(e) {
            const rating = document.getElementById('rating').value;
            const comment = document.getElementById('comment').value.trim();
            if (!rating) {
                alert('Please select a rating.');
                e.preventDefault();
            } else if (comment.length < 5) {
                alert('Comment must be at least 5 characters long.');
                e.preventDefault();
            }
        });
    }

    // 3. User Registration Validation
    const registerForm = document.querySelector('form[action="/register"]');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword')?.value;

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                alert('Please enter a valid email address.');
                e.preventDefault();
                return;
            }

            if (password.length < 6) {
                alert('Password must be at least 6 characters long.');
                e.preventDefault();
                return;
            }

            if (confirmPassword && password !== confirmPassword) {
                alert('Passwords do not match.');
                e.preventDefault();
            }
        });
    }
});
