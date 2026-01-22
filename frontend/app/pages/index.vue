<template>
  <div class="welcome-page">
    <div class="hero-section">
      <div class="container">
        <div class="hero-content">
          <h1 class="main-title">Scientific Publications Platform</h1>
          <p class="tagline">Research Center XYZ</p>
          <p class="description">
            Manage and discover scientific publications across multiple research areas.
            Collaborate with your team, share insights, and stay updated on the latest research.
          </p>
          <div class="cta-buttons">
            <nuxt-link to="/auth/login" class="btn-login">Login to Platform</nuxt-link>
          </div>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="container">
        <h2>Platform Features</h2>
        <div class="features-grid">
          <div class="feature-card">
            <h3>Publication Management</h3>
            <p>Upload and organize scientific publications, articles, conference papers, patents, datasets, and more.</p>
          </div>

          <div class="feature-card">
            <h3>Smart Tagging</h3>
            <p>Tag publications by project, area, or topic. Subscribe to tags and get notified of new additions.</p>
          </div>

          <div class="feature-card">
            <h3>Collaboration</h3>
            <p>Add comments, ratings, and critical analyses to publications. Share insights with your team.</p>
          </div>

          <div class="feature-card">
            <h3>Advanced Search</h3>
            <p>Search by title, author, scientific area, tags, date, and more. Filter and sort results easily.</p>
          </div>

          <div class="feature-card">
            <h3>AI-Powered Summaries</h3>
            <p>Automatically generate summaries of publications using advanced language models.</p>
          </div>

          <div class="feature-card">
            <h3>Real-time Notifications</h3>
            <p>Get email alerts when publications with your subscribed tags are added or updated.</p>
          </div>
        </div>
      </div>
    </div>

    <div class="research-areas">
      <div class="container">
        <h2>Research Areas</h2>
        <div class="areas-list">
          <span class="area-badge">Data Science</span>
          <span class="area-badge">Materials Science</span>
          <span class="area-badge">Artificial Intelligence</span>
          <span class="area-badge">Machine Learning</span>
          <span class="area-badge">Biotechnology</span>
          <span class="area-badge">Nanotechnology</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Global font and background */
.welcome-page {
  font-family: "Inter", sans-serif;
  background: #f9fafb;
  color: #1e293b;
  min-height: 100vh;
}

/* Container */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Hero Section */
.hero-section {
  padding: 100px 0;
  text-align: center;
}

.main-title {
  font-size: 52px;
  font-weight: 700;
  margin-bottom: 16px;
}

.tagline {
  font-size: 22px;
  color: #475569;
  margin-bottom: 24px;
}

.description {
  font-size: 18px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 40px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
}

.btn-login {
  padding: 14px 40px;
  background: #3b82f6;
  color: white;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
}

.btn-login:hover {
  background: #2563eb;
}

/* Features Section */
.features-section {
  padding: 80px 0;
}

.features-section h2 {
  text-align: center;
  font-size: 32px;
  margin-bottom: 50px;
  color: #1e293b;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.feature-card {
  background: white;
  border: 1px solid #e2e8f0;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 12px;
  color: #1e293b;
}

.feature-card p {
  font-size: 14px;
  line-height: 1.6;
  color: #475569;
}

/* Research Areas */
.research-areas {
  padding: 60px 0;
  text-align: center;
}

.research-areas h2 {
  font-size: 28px;
  margin-bottom: 30px;
}

.areas-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.area-badge {
  background: #e2e8f0;
  color: #1e293b;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .main-title {
    font-size: 36px;
  }

  .tagline {
    font-size: 18px;
  }

  .description {
    font-size: 16px;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
