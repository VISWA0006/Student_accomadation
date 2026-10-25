
function updateGlobalUnreadCounter() {
    fetch('/notifications/api/unread-count')
        .then(response => {
            if (!response.ok) return null;
            return response.json();
        })
        .then(data => {
            if (data && data.unreadCount !== undefined) {
                const navBadge = document.getElementById('navNotifBadge');
                const sidebarBadge = document.getElementById('sidebarNotifBadge');
                const displayCount = data.unreadCount;

                if (navBadge) {
                    if (displayCount > 0) {
                        navBadge.textContent = displayCount > 99 ? '99+' : displayCount;
                        navBadge.style.display = 'flex';
                    } else {
                        navBadge.style.display = 'none';
                    }
                }

                if (sidebarBadge) {
                    if (displayCount > 0) {
                        sidebarBadge.textContent = displayCount;
                        sidebarBadge.style.display = 'inline-block';
                    } else {
                        sidebarBadge.style.display = 'none';
                    }
                }
            }
        })
        .catch(err => {

        });
}

document.addEventListener('DOMContentLoaded', () => {

    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

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

    updateGlobalUnreadCounter();
});
