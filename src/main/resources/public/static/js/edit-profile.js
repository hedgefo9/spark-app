const userId = document.querySelector('meta[name="user-id"]').content;
const token = document.querySelector('meta[name="csrf-token"]').content;

let currentIndex = 0;
let photos = [];
const currentPhoto = document.getElementById('current-photo');
const primaryButton = document.getElementById('primary-button');
const uploadForm = document.getElementById('upload-form');
let currentPhotoId = null;


Функция для получения фотографий пользователя
function loadPhotos(userId) {
    fetch(`/photos/${userId}`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': token
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка загрузки фотографий');
            }
            return response.json();
        })
        .then(data => {
            photos = data.map(photo => ({
                id: photo.photoId,
                src: `/files/static/images/uploads/${photo.fileName}`,
                isPrimary: photo.isPrimary
            }));

            if (photos.length > 0) {
                const primaryPhotoIndex = photos.findIndex(photo => photo.isPrimary);
                if (primaryPhotoIndex !== -1) {
                    currentIndex = primaryPhotoIndex;
                } else {
                    currentIndex = 0;
                }
                updatePhoto();
            } else {
                console.log('Нет доступных фотографий');
            }
        })
        .catch(error => console.error('Ошибка:', error));
}

Обновление отображаемого фото
function updatePhoto() {
    const photo = photos[currentIndex];
    currentPhoto.src = photo.src;
    currentPhotoId = photo.id;
    primaryButton.classList.toggle('primary', photo.isPrimary);
}

Переключение на предыдущее фото
function prevPhoto() {
    currentIndex = (currentIndex - 1 + photos.length) % photos.length;
    updatePhoto();
}

Переключение на следующее фото
function nextPhoto() {
    currentIndex = (currentIndex + 1) % photos.length;
    updatePhoto();
}

Удаление фото
function deletePhoto(photoId) {
    fetch(`/photos/${photoId}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': token
        }
    })
        .then(response => {
            if (response.ok) {
                photos = photos.filter(photo => photo.id !== photoId);
                currentIndex = Math.min(currentIndex, photos.length - 1);
                updatePhoto();
            } else {
                alert('Ошибка при удалении фото');
            }
        })
        .catch(error => console.error('Ошибка:', error));
}

Установка главного фото
function setPrimaryPhoto(photoId) {
    fetch(`/photos/${userId}/primary/${photoId}`,
        {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': token
            }
        })
        .then(response => {
            if (response.ok) {
                photos.forEach(photo => (photo.isPrimary = photo.id === photoId));
                updatePhoto();
            } else {
                alert('Ошибка при установке главного фото');
            }
        })
        .catch(error => console.error('Ошибка:', error));
}

Обработчик отправки формы для загрузки нового фото
uploadForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(uploadForm);

    if (photos.length === 0) {
        formData.set("isPrimary", true);
    }
    try {
        const response = await fetch('/photos/upload', {
            method: 'POST',
            body: formData,
            headers: {
                'X-CSRF-TOKEN': token
            }
        });

        if (response.ok) {
            loadPhotos(userId);
        } else {
            alert('Ошибка при загрузке фото');
        }
    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось загрузить фото');
    }
});

loadPhotos(userId);

document.addEventListener("DOMContentLoaded",  () => {
    document.getElementById("save-profile-btn").addEventListener("click", () => {
        const profileData = {
            user_id: userId,
            first_name: document.getElementById("firstName").value,
            last_name: document.getElementById("lastName").value,
            gender: document.getElementById("gender").value,
            birth_date: document.getElementById("birthDate").value,
            phone_number: document.getElementById("phoneNumber").value,
            email: document.getElementById("email").value,
            city: document.getElementById("city").value,
            education: document.getElementById("education").value,
            smokes: document.getElementById("smokes").value === "true"
        };

        fetch("/user", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                'X-CSRF-TOKEN': token
            },
            body: JSON.stringify(profileData)
        })
            .then(response => response.ok ? alert("Профиль успешно обновлен!") : alert("Ошибка при обновлении профиля!"))
            .catch(error => console.error("Ошибка:", error));
    });

    Обработка сохранения био
    document.getElementById("save-bio-btn").addEventListener("click", () => {
        const bioData = {
            userId: userId,
            aboutMe: document.getElementById("aboutMe").value,
            lookingFor: document.getElementById("lookingFor").value
        };

        fetch("/user/bio", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                'X-CSRF-TOKEN': token
            },
            body: JSON.stringify(bioData)
        })
            .then(response => response.ok ? alert("Био успешно обновлено!") : alert("Ошибка при обновлении био!"))
            .catch(error => console.error("Ошибка:", error));
    });


});
