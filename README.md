# ChronoSync

ChronoSync is a milk delivery management system built to simplify how customers and vendors handle daily milk subscriptions. It allows users to manage deliveries, pause services when needed, and track activity in a simple way.

This is a full-stack project with a React frontend and a Spring Boot backend.

---

## Live Links

Frontend:
https://chronosync-app-xi.vercel.app

Backend:
https://chronosync-docker.onrender.com

---

## About the Project

The main goal of this project is to make milk delivery flexible and easy to manage.

Customers can subscribe to milk delivery and pause it whenever they want. The system automatically adjusts the subscription based on those changes. Vendors can view customer data and track delivery activity.

---

## Features

Customer:
- View subscription details
- Pause and resume delivery
- Track activity history

Vendor:
- View customer list
- Monitor deliveries
- See basic analytics

System:
- Tracks all actions (pause, resume, etc.)
- Adjusts subscription dates automatically
- Basic role-based access

---

## Tech Stack

Frontend:
- React (Vite)
- React Router

Backend:
- Spring Boot
- Spring Security (JWT)
- JPA / Hibernate

Database:
- MySQL

Deployment:
- Frontend hosted on Vercel
- Backend hosted on Render

---

## How to Run Locally

Clone the repository:

git clone <your-repo-link>
cd ChronoSync

Run backend:

cd backend
./mvnw spring-boot:run

Run frontend:

cd frontend
npm install
npm run dev

---

## Notes

- Authentication is handled using JWT tokens
- Customer and vendor dashboards are separate
- Vendor dashboard may show empty if no vendor data is available

---

## Challenges Faced

- Handling CORS between frontend and backend
- Fixing routing issues on deployment
- Managing pause and resume logic correctly
- Deploying frontend and backend separately

---

## Future Improvements

- Improve UI design
- Add notifications
- Add payment integration
- Make it mobile friendly

---

## Summary

This project helped in understanding full-stack development, API integration, and deployment. It also involved solving real issues like CORS errors and routing problems.
