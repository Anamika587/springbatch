package demo.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import demo.model.Person;

public class PersonRowMapper implements RowMapper<Person> {

	@Override
	public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
		Person person = new Person();
		
		person.setFirstName(rs.getString("first_name"));
		person.setLastName(rs.getString("last_name"));
		
		return person;
	}
 
}
