package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Author;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class AuthorDaoHibernate implements AuthorDao {

    private final EntityManagerFactory emf;

    public AuthorDaoHibernate(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Author getById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return getEntityManager().find(Author.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Author findAuthorByName(String firstName, String lastName) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Author> query = em.createQuery("SELECT a FROM Author a " +
                    "WHERE a.firstName = :first_name and a.lastName = :last_name", Author.class);
            query.setParameter("first_name", firstName);
            query.setParameter("last_name", lastName);

            return query.getSingleResult();
        } finally {
            em.close();
        }

    }

    @Override
    public Author saveNewAuthor(Author author) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(author);
            em.flush();
            em.getTransaction().commit();
            return author;
        } finally {
            em.close();
        }
    }

    @Override
    public Author updateAuthor(Author author) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(author);
            em.flush();
            em.clear();
            Author savedAuthor = em.find(Author.class, author.getId());
            em.getTransaction().commit();
            return savedAuthor;
        } finally {
            em.close();
        }

    }

    @Override
    public void deleteAuthorById(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Author author = em.find(Author.class, id);
            em.remove(author);
            em.flush();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Author> findAllAuthorsByLastName(String lastName, Pageable pageable) {
        EntityManager em = getEntityManager();
        try {
            String hql = "SELECT a FROM Author a WHERE a.lastName = :lastName";
            pageable.getSort();
            if (pageable.getSort().getOrderFor("firstname") != null) {
                hql = hql + " order by a.firstName "
                        + pageable
                        .getSort()
                        .getOrderFor("firstname")
                        .getDirection()
                        .name();
            }
            TypedQuery<Author> query = em.createQuery(hql, Author.class);
            query.setParameter("lastName", lastName);
            query.setFirstResult(Math.toIntExact(pageable.getOffset()));
            query.setMaxResults(pageable.getPageSize());
            return query.getResultList();
        } finally {
            em.close();
        }

    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
