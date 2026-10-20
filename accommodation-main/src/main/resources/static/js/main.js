// Student Accommodation Management System - Client Script

document.addEventListener('DOMContentLoaded', () => {
    // 1. Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // 2. Client-side room availability hints on booking form
    const roomSelect = document.getElementById('roomSelect');
    const roomHint = document.getElementById('roomHint');
    if (roomSelect && roomHint) {
        roomSelect.addEventListener('change', (e) => {
            const selectedOption = e.target.selectedOptions[0];
            if (selectedOption && selectedOption.dataset.info) {
                roomHint.textContent = selectedOption.dataset.info;
                roomHint.style.display = 'block';
            } else {
                roomHint.style.display = 'none';
            }
        });
    }
});
